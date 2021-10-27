import numpy as np
import pandas as pd
import re
import time, itertools, functools, datetime
import os, copy
import inspect 
from inspect import signature
import copy
import datetime 
import json 
from sklearn.linear_model import LogisticRegression
import math
from warnings import warn
import random
from sklearn.metrics import roc_auc_score

def pd_to_est(x):
    return math.log(x/(1-x))
        
def est_to_pd(x):
    return 1/(1+np.exp(-x))

def gini_model(score,target):
    return 2*roc_auc_score(np.array(target), np.array(score))-1

def early_repayment(x,y):
    if x.month == y.month and x.year == y.year:
        return 1
    else:
        return 0 

def in_datetime(x):
    return datetime(datetime(x.year, x.month, x.day))    
    
    
class DataInfo:
    def __init__(self,data):
        self.data_col = list(data.columns)
        self.data_len = data.shape[0]
        
class SequentialTransformer:
    '''
    SequentialTransformer - transformer for unioning transformers
    '''
    def __init__(self,*tuple_list):
        self.stages_ = tuple_list
    
    def fit(self, ds):
        ds_ = ds.copy()
        self.tmp_ = []
        for i in self.stages_:
            if isinstance(i, LogRegTransformer) == False:
                ds_ = i.transform(ds_)
                self.tmp_.append(i)
            else:
                i_ = i.fit(ds_)
                ds_ = i_.transform(ds_)
                self.tmp_.append(i_)
        return self
        
    def transform(self, ds):
        ds_1 = ds.copy()
        self.fitted_model = SequentialTransformer(*self.tmp_)
        for i in range(len(self.fitted_model.stages_)):
            if i == 0:
                ds_1 = self.fitted_model.stages_[i].transform(ds.copy())
            else:
                ds_1 = self.fitted_model.stages_[i].transform(ds_1.copy()) 
        return ds_1

class DeploymentSequentialTransformer:
    def __init__(self, transformer):
        self.transformer_ = transformer
    
    def deploy_transformer(self):
        self.new_tr = []
        if type(self.transformer_) == SequentialTransformer:
            for i in self.transformer_.stages_:
                if (hasattr(i, 'stages_') == False) or type(i) == MergeTransformer:
                    self.new_tr.append(i)
                else:
                    for val in i.stages_[0]:
                        self.new_tr.append(val)
        else:
            self.new_tr = self.transformer_ 
        return self
    
    def change_pipe(self):
        return SequentialTransformer(*self.new_tr)
    
        
class ColumnTransformer:
    def __init__(self,aggrs):
        if type(aggrs) != dict:
            raise TypeError('aggrs should be dict !')
        self.cols_1 = [v[0] for k,v in aggrs.items()] 
        self.function = [v[1] for k,v in aggrs.items()] 
        self.new_col = [k for k,v in aggrs.items()] 
        
    def fit(self, ds):
        return self
    
    def func_repeat(self):
        return len(self.cols_1)

    def transform(self, ds):        
        if self.func_repeat() == 1:
            if set(self.cols_1[0]).issubset(DataInfo(ds).data_col) == False:
                raise Exception(f'{self.requiredSchema()} should be in input dataframe for transform')
            else:
                if set(self.new_col).issubset(DataInfo(ds).data_col) == True:
                    new_col_ = self.function[0](*[ds[x].values for x in self.cols_1[0]])
                    del ds[self.new_col[0]]
                    ds[self.new_col] = new_col_
                else:    
                    new_col_ = self.function[0](*[ds[x].values for x in self.cols_1[0]])
                    ds[self.new_col] = new_col_
                                
        elif self.func_repeat() > 1:
            for i in range(self.func_repeat()):
                if set(self.cols_1[i]).issubset(DataInfo(ds).data_col) == False:
                    raise Exception(f'{self.requiredSchema()} should be in input dataframe for transform')
                else:
                    if set(self.new_col[i]).issubset(DataInfo(ds).data_col) == True:
                        if len(self.cols_1[i]) == 0:
                            new_col_ = np.array([self.function[i] for x in range(DataInfo(ds).data_len)])
                            del ds[self.new_col[i]]
                            ds[self.new_col[i]] = new_col_
                        else:    
                            new_col_ = self.function[i](*[ds[x].values for x in self.cols_1[i]])
                            del ds[self.new_col[i]]
                            ds[self.new_col[i]] = new_col_
                    else:
                        if len(self.cols_1[i]) == 0:
                            new_col_ = np.array([self.function[i] for x in range(DataInfo(ds).data_len)])
                            ds[self.new_col[i]] = new_col_
                        else:    
                            new_col_ = self.function[i](*[ds[x].values for x in self.cols_1[i]])
                            ds[self.new_col[i]] = new_col_
        else:
            pass
        
        return ds
    
    def requiredSchema(self):
        return self.cols_1
    
    def transformSchema(self):
        return self.new_col
    
    
class RenameTransformer:  
    def __init__(self,aggrs):
        if type(aggrs) != dict:
            raise TypeError('aggrs should be dict !')
        self.cols_old = [k for k,v in aggrs.items()] # old column name
        self.cols_new = [v for k,v in aggrs.items()] # new column name
        
    def fit(self, ds):
        return self
    
    def transform(self, ds):
        if set(self.cols_old).issubset(DataInfo(ds).data_col) == False:
            raise Exception(f'{self.requiredSchema()} should be in input dataframe for transform')
        else:
            for i in range(len(self.cols_old)):
                ds = ds.rename(columns = {self.cols_old[i]: self.cols_new[i]})
        return ds
    
    def requiredSchema(self):
        return self.cols_old
    
    def transformSchema(self):
        return self.cols_new
    
    
class FilterTransformer:
    def __init__(self,aggrs):
        if type(aggrs) != tuple:
            raise TypeError('aggrs should be tuple !')
        self.cols_1_0 = aggrs[0] # filter column 
        self.function = aggrs[1] # function in args
        
    def fit(self, ds):
        return self
    
    def transform(self, ds):
        if set([self.cols_1_0]).issubset(DataInfo(ds).data_col) == False:
            raise Exception(f'{self.requiredSchema()} should be in input dataframe for transform')
        else:
            new_col_ = self.function(*[ds[self.cols_1_0]])
            ds['tmp_flg'] = new_col_
            ds_1 = ds[ds['tmp_flg'] == True].reset_index().drop(columns = ['index'])
            del ds_1['tmp_flg']
        return ds_1
        
    def requiredSchema(self):
        return self.cols_1_0
    
    
class Separate2UniqueTransformer:

    def __init__(self,required_columns):
        if (type(required_columns) != list):
            raise TypeError('by should be list or str !')    
        self._required_columns = required_columns
    
    def fit(self, ds):
        return self
    
    def transform(self,ds):        
        tmp = ds[self._required_columns].copy()
        tmp['union_col'] = [(x,y) for x,y in zip(tmp[self._required_columns[0]],tmp[self._required_columns[1]])]
        tuple_1 = tuple(np.unique(tmp['union_col']))
        
        tmp_1 = pd.DataFrame(list(np.unique(tmp['union_col'])))
        tmp_1 = tmp_1.rename(columns = {0:self._required_columns[0], 1:self._required_columns[1]})
            
        return tmp_1
        

class GroupbyTransformer:

    def __init__(self,by, need_columns, types, rules = None):
        if (type(by) != list) & (type(by) != str):
            raise TypeError('by should be list or str !')
            
        if (type(need_columns) != list) & (type(need_columns) != str):
            raise AttrValue('need_columns should be list or str !')
            
        if types not in ('sum', 'min','max'):
            raise TypeError('types should be in (''sum'', ''min'',''max'') !')
        
        self._rules = rules
        
        if self._rules is not None:
            self._rules_col = rules[0] #col
            self._rules_rul = rules[1] #rule for col
        else:
            pass
        
        self._types = types
        self._by_list = by 
        self._need_columns = need_columns
    
    def fit(self, ds):
        return self        
        
    def transform(self,ds):
        
        if self._rules is None:
            if type(self._by_list) == str:
                if self._types == 'sum':
                    return ds[[self._by_list]+self._need_columns].groupby(by = [self._by_list]).sum().reset_index()
                
                elif self._types == 'min':
                    return ds[[self._by_list]+self._need_columns].groupby(by = [self._by_list]).min().reset_index()
                elif self._types == 'max':
                    return ds[[self._by_list]+self._need_columns].groupby(by = [self._by_list]).max().reset_index()
            else:
                if self._types == 'sum':
                    return ds[[self._by_list]+self._need_columns].groupby(by = self._by_list).sum().reset_index()
                elif self._types == 'min':
                    return ds[[self._by_list]+self._need_columns].groupby(by = self._by_list).min().reset_index()
                
                elif self._types == 'max':
                    return ds[[self._by_list]+self._need_columns].groupby(by = self._by_list).max().reset_index()
                
        else:
            if type(self._by_list) == str:
                if self._types == 'sum':
                    return ds[ds[self._rules_col]!=self._rules_rul][[self._by_list]+self._need_columns].groupby(by = [self._by_list]).sum().reset_index()
                
                elif self._types == 'min':
                    return ds[ds[self._rules_col]!=self._rules_rul][[self._by_list]+self._need_columns].groupby(by = [self._by_list]).min().reset_index()
                elif self._types == 'max':
                    return ds[ds[self._rules_col]!=self._rules_rul][[self._by_list]+self._need_columns].groupby(by = [self._by_list]).max().reset_index()
            else:
                if self._types == 'sum':
                    return ds[ds[self._rules_col]!=self._rules_rul][[self._by_list]+self._need_columns].groupby(by = self._by_list).sum().reset_index()
                elif self._types == 'min':
                    return ds[ds[self._rules_col]!=self._rules_rul][[self._by_list]+self._need_columns].groupby(by = self._by_list).min().reset_index()
                
                elif self._types == 'max':
                    return ds[ds[self._rules_col]!=self._rules_rul][[self._by_list]+self._need_columns].groupby(by = self._by_list).max().reset_index()
            
            
            
class MergeTransformer:

    def __init__(self,key,*tuple_list):
        if type(key) != str:
            raise TypeError('by should be str !')
        self.key = key 
        self.stages_ = tuple_list[0]
        
    def fit(self, ds):
        return self
    
    def transform(self, ds):
        var = {}
        
        for i in range(len(self.stages_)):
            self._tmp = self.stages_[i].transform(ds.copy())
            var[i] = self._tmp
        
        self._final = var[0] 
        
        for i in range(1,len(self.stages_)):
            self._final = self._final.merge(var[i], how = 'left', on = self.key)
            
        return self._final
    
class TypeDeltaTransformer:
    def __init__(self,column):
        if type(column) != str:
            raise TypeError('by should be str !')
        self.column = column 
        
    def fit(self, ds):
        return self
    
    def transform(self, ds):
        ds[self.column] = round(ds[self.column].astype('timedelta64[D]')/30,0)
        return ds
    

class LogRegTransformer:
    def __init__(self,target, list_variable, out_prob):
        self.target = target
        self.list_variable = list_variable
        self.out_prob = out_prob
        self.model = LogisticRegression(random_state = 0, C = 100,solver = 'liblinear')
        
    def __repr__(self):
        return f'logreg model: variable - {self.list_variable}, out features - {self.out_prob}'
        
    def fit(self, ds):
        self.model_fitted = self.model.fit(np.array(ds[self.list_variable]), np.array(ds[self.target]))
        return self
    
    def fitted_coef(self):
        return self.model_fitted
        
    def transform(self, ds):
        ds[self.out_prob] = self.model_fitted.predict_proba(np.array(ds[self.list_variable]))[:,-1]
        return ds 


class CrossValScore:
    def __init__(self, model, ds, cv, target):
        self.model = model 
        self.ds = ds
        self.cv = cv
        self.target = target
        
    def validation(self):
        ds_1 = self.ds.reset_index()
        data_empty_test = pd.DataFrame(columns = ds_1.columns).reset_index()
        
        self.step = int(np.round(DataInfo(ds_1).data_len / self.cv))
        self.len = DataInfo(ds_1).data_len
    
        total_res = []
        
        for _ in range(self.cv): 
            list_test = [] 
            list_ = list(np.arange(0, DataInfo(ds_1).data_len, 1))
            
            for i in range(self.step):
                intermediate = random.choice(list_)
                list_test.append(intermediate)
                list_.remove(intermediate)
             
            data_empty_test = ds_1[ds_1.index.isin(list_test)]
            data_empty_train = ds_1[ds_1.index.isin(list_)]    
            model_1 = self.model.fit(data_empty_train.copy())
            res_ = model_1.transform(data_empty_test.copy())
            intermediate_score = gini_model(res_['MODEL_PD'], res_[self.target])
            total_res.append(intermediate_score)
        return total_res
        
        
        
    
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
    
                           
    
        
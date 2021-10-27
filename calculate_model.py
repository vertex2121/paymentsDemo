import dill as pickle
import owner_columns
import pandas as pd
import org.slf4j.LoggerFactory

LOGGER = LoggerFactory.getLogger("jython.scorecard.auto_base_scorecard.calculate_model");

def run(runId, clientage_v, loanterm_v, clientjobperiod_v, bankproductname_v, marriedstatus_v, workposition_v, factaddressregion_v, childrencount_v):

	LOGGER.info("RUN_ID=%d Start execution", runId)
	
	try:
		d = {
		'Clientage': [clientage_v], 
		'loanterm': [loanterm_v], 
		'clientjobperiod': [clientjobperiod_v], 
		'bankproductname': [bankproductname_v], 
		'marriedstatus': [marriedstatus_v], 
		'workposition': [workposition_v], 
		'factaddressregion': [factaddressregion_v], 
		'childrencount': [childrencount_v]
		}

		with open('model_base_card.pickle', 'rb') as model_p:
			model = pickle.load(model_p)
		pd = model.transform(pd.DataFrame(data=d))
		LOGGER.info("RUN_ID=%d End execution. Pd is %d", runId)
		
	except (Exception, java.lang.Exception) as e:
		LOGGER.error("RUN_ID=%d Error occurred. Error is %s", e.getMessage())
	
	return pd
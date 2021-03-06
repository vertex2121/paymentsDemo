/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.vpb.payment;

import java.math.BigDecimal;
import ru.vpb.payment.model.Payment;
import ru.vpb.payment.paymentService.PaymentService;

/**
 *
 * В задаче предполагается наличие некоторой системы приема платежей. 
 * При попадании в систему платеж передается на обработку. 
 * Платежи осуществляются на сумму за некоторую услугу в пользу клиента с лицевым счетом.
 
 * Задача: 
 * В эту систему нужно добавить подсистему ограничений (лимитов) на прием платежей для борьбы с мошенничеством.
 * В решении должна быть возможность определять, например, такие лимиты для платежей:
 * 1. Не более 5000 руб. днем с 9:00 утра до 23:00 за одну услугу(*);
 * 2. Не более 1000 руб. ночью с 23:00 до 9:00 утра за одну услугу(*);
 * 3. Не более 2000 руб. в сутки по одинаковым платежам(**);
 * 4. Не более 3000 руб. в течение одного часа за одну услугу(*);
 * 5. Не более 20 одинаковых платежей(**) в сутки;
 * 6. Не более 30 платежей не более чем на 4000 руб.(***) с 10:00 до 17:00 за одну услугу(*);
 * 7. Не более 10 платежей не более чем на 3000 руб.(***) в течение двух часов одним клиентом.
 * (*) услуга - это, например, пополнение счета мобильного телефона (лицевой счет клиента) провайдера МТС;
 * (**) одинаковые платежи - платежи с одинаковыми счетом клиента и услугой;
 * (***) сумма указана для совокупности платежей.
 
 * Лимиты должны быть настраиваемые по всем параметрам.
 * В случае, если какой-то из лимитов превышен, необходимо переводить "подозрительный" платеж 
 * в статус "требует подтверждения". Платеж, прошедший ограничения, должен быть переведен в статус "готов к проведению".
 
 * В решении задачи должны быть юнит-тесты для каждого лимита. 
 * Для тестов нужно реализовать примитивную систему приема платежей, в которую в юнит-тестах должны поступать платежи для проверки работоспособности лимитов из списка выше. 
 * Дополнительно должен быть реализован хотя бы один юнит-тест, в котором система приема платежей настроена с несколькими разными лимитами одновременно. 
 * Для юнит-тестов системе должны быть известны несколько (2-3) клиентов и несколько (2-3) услуг.
 * 
 * Требования к решению:
 * - Задачу необходимо решить на языке Java;
 * - Требуется реализовать задачу с юнит-тестами, используя JUnit;
 * - Решение должно быть реализовано в парадигме ООП с использованием паттернов проектирования;
 * - Все необходимые системы, указанные в задаче, должны быть простыми объектами без использования сторонних технологий;
 * - В случае, если требуется внешняя библиотека, например, для работы с датой и временем, проект должен использовать Maven или Gradle.
 * 
 * Не нужно использовать в решении задачи:
 * - Базы данных;
 * - Чтение из файлов;
 * - Сетевое взаимодействие;
 * - Многопоточность;
 * - Библиотеки для построения графического пользовательского интерфейса.
 * 
 * @author vertex21
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    
    private static final PaymentService paymentService = new PaymentService();
    
    public static void main(String[] args) {
        Payment payment1 = new Payment(1L, 100L, new BigDecimal(1000.00), "Payment to MTS", "89859621478");
        Payment payment2 = new Payment(2L, 101L, new BigDecimal(2000.00), "Payment to Megafon", "89859621479");
        Payment payment3 = new Payment(3L, 102L, new BigDecimal(3000.00), "Payment to MTS", "89859621470");
        Payment payment4 = new Payment(4L, 102L, new BigDecimal(3000.00), "Payment to MTS", "89859621470");
        paymentService.addPayToQueue(payment1);
        paymentService.addPayToQueue(payment2);
        paymentService.addPayToQueue(payment3);
        paymentService.addPayToQueue(payment4);
        paymentService.pay();
        paymentService.checkDelayed();
    }
}

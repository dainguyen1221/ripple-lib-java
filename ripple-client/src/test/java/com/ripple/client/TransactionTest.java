
package com.ripple.client;

import com.ripple.config.Config;
import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.types.known.tx.signed.SignedTransaction;
import com.ripple.core.types.known.tx.txns.Payment;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class TransactionTest {
    {
        Config.initBouncy();
    }

    @Test
    public void testCreatePaymentTransaction() throws Exception {
        String secret = "ssStiMFzkGefDoTqgk9w9WpYkTepQ";
        Payment payment = new Payment();

        // Put `as` AccountID field Account, `Object` o
        payment.as(AccountID.Account,     "rGZG674DSZJfoY8abMPSgChxZTJZEhyMRm");
        payment.as(AccountID.Destination, "rPMh7Pi9ct699iZUTWaytJUoHcJ7cgyziK");
        payment.as(Amount.Amount,         "1000000000");
        payment.as(Amount.Fee,            "10000");
        payment.as(UInt32.Sequence,       10);

        SignedTransaction sign = payment.sign(secret);
        assertTrue(sign.txn.verifySignature(payment.account()));

        AccountID someOtherKey = AccountID.fromPassPhrase("A1");
        assertFalse(sign.txn.verifySignature(someOtherKey));
    }
}

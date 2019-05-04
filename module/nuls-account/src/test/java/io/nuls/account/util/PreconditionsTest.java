package io.nuls.account.util;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertTrue;

public class PreconditionsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void checkArgument() {
    }

    @Test
    public void checkNotNull() {
    }

    @Test
    public void checkNotEmpty() {
    }

    @Test
    public void checkNotNull1() {
        boolean test = false;
        try {
            Preconditions.checkNotNull(null, AccountErrorCode.NULL_PARAMETER);
        } catch (NulsRuntimeException e) {
            test = true;
        }
        assertTrue(test);
        test = false;
        try {
            Preconditions.checkNotNull(new Object[]{"1", null, "2"}, AccountErrorCode.NULL_PARAMETER);
        } catch (NulsRuntimeException e) {
            test = true;
        }
        assertTrue(test);
        test = false;
        Preconditions.checkNotNull(new Object[]{"1", "2"}, AccountErrorCode.NULL_PARAMETER);

    }
}
package com.ripple.client.transactions;

import com.ripple.client.pubsub.CallbackContext;
import com.ripple.client.pubsub.Publisher;
import com.ripple.client.pubsub.Publisher.Callback;
import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.types.known.tx.Transaction;
import com.ripple.core.types.known.tx.result.TransactionResult;
import com.ripple.core.types.known.tx.signed.SignedTransaction;

import java.util.ArrayList;
import java.util.TreeSet;

public class ManagedTxn extends SignedTransaction {
    public interface events<T> extends Callback<T> {}
    public interface OnSubmitSuccess extends events<Response> {}
    public interface OnSubmitFailure extends events<Response> {}
    public interface OnSubmitError extends events<Response> {}
    public interface OnTransactionValidated extends events<TransactionResult> {}

    public ManagedTxn onSubmitFailure(OnSubmitFailure onSubmitFailure) {
        on(OnSubmitFailure.class, onSubmitFailure);
        return this;
    }
    public ManagedTxn onceSubmitFailure(OnSubmitFailure onSubmitFailure) {
        once(OnSubmitFailure.class, onSubmitFailure);
        return this;
    }
    public ManagedTxn onSubmitSuccess(OnSubmitSuccess onSubmitSuccess) {
        on(OnSubmitSuccess.class, onSubmitSuccess);
        return this;
    }
    public ManagedTxn onceSubmitSuccess(OnSubmitSuccess onSubmitSuccess) {
        once(OnSubmitSuccess.class, onSubmitSuccess);
        return this;
    }
    public ManagedTxn onSubmitError(OnSubmitError onSubmitError) {
        on(OnSubmitError.class, onSubmitError);
        return this;
    }
    public ManagedTxn onceSubmitError(OnSubmitError onSubmitError) {
        once(OnSubmitError.class, onSubmitError);
        return this;
    }
    public ManagedTxn onTransactionValidated(OnTransactionValidated onTransactionValidated) {
        on(OnTransactionValidated.class, onTransactionValidated);
        return this;
    }
    public ManagedTxn onceTransactionValidated(OnTransactionValidated onTransactionValidated) {
        once(OnTransactionValidated.class, onTransactionValidated);
        return this;
    }

    public TransactionResult result;

    public ManagedTxn onValidated(final Callback<ManagedTxn> handler) {
        on(OnTransactionValidated.class, args -> {
            result = args;
            handler.called(ManagedTxn.this);
        });
        return this;
    }

    public ManagedTxn onError(final Callback<ManagedTxn> cb) {
        on(OnSubmitFailure.class, args -> cb.called(ManagedTxn.this));
        on(OnSubmitError.class, args -> cb.called(ManagedTxn.this));
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends events> boolean removeListener(Class<T> key, Callback cb) {
        return publisher.removeListener(key, cb);
    }

    @SuppressWarnings("unchecked")
    public <T extends events> int emit(Class<T> key, Object args) {
        return publisher.emit(key, args);
    }

    @SuppressWarnings("unchecked")
    public <T extends events> void once(Class<T> key, CallbackContext executor, T cb) {
        publisher.once(key, executor, cb);
    }

    @SuppressWarnings("unchecked")
    public <T extends events> void once(Class<T> key, T cb) {
        publisher.once(key, cb);
    }

    @SuppressWarnings("unchecked")
    public <T extends events> void on(Class<T> key, CallbackContext executor, T cb) {
        publisher.on(key, executor, cb);
    }

    @SuppressWarnings("unchecked")
    public <T extends events> void on(Class<T> key, T cb) {
        publisher.on(key, cb);
    }

    public Publisher<events> publisher() {
        return publisher;
    }

    private boolean isSequencePlug;
    public boolean isSequencePlug() {
        return isSequencePlug;
    }
    public void setSequencePlug(boolean isNoop) {
        this.isSequencePlug = isNoop;
        description("SequencePlug");
    }

    private String description;
    public String description() {
        if (description == null) {
            return txn.transactionType().toString();
        }
        return description;
    }
    public void description(String description) {
        this.description = description;
    }

    public ManagedTxn(Transaction txn) {
        this.txn = txn;
    }
    private final Publisher<events> publisher = new Publisher<>();
//    private final MyTransaction publisher = new MyTransaction();
    private boolean finalized = false;

    public boolean responseWasToLastSubmission(Response res) {
        Request req = lastSubmission().request;
        return res.request == req;
    }

    public boolean finalizedOrResponseIsToPriorSubmission(Response res) {
        return isFinalized() || !responseWasToLastSubmission(res);
    }

    public ArrayList<Submission> submissions = new ArrayList<>();

    public Submission lastSubmission() {
        if (submissions.isEmpty()) {
            return null;
        } else {
            return submissions.get(submissions.size() - 1);
        }
    }
    private TreeSet<Hash256> submittedIDs = new TreeSet<>();

    public boolean isFinalized() {
        return finalized;
    }

    public void setFinalized() {
        finalized = true;
    }

    public void trackSubmitRequest(Request submitRequest, long ledger_index) {
        Submission submission = new Submission(submitRequest,
                                               sequence(),
                                               hash,
                                               ledger_index,
                                               txn.get(Amount.Fee),
                                               txn.get(UInt32.LastLedgerSequence));
        submissions.add(submission);
        trackSubmittedID();
    }

    public void trackSubmittedID() {
        submittedIDs.add(hash);
    }

    boolean wasSubmittedWith(Hash256 hash) {
        return submittedIDs.contains(hash);
    }

    public UInt32 sequence() {
        return txn.sequence();
    }
}

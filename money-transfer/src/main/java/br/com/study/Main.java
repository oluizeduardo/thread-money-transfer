package br.com.study;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        Money money1 = new Money(new BigDecimal("1000"));
        Money money2 = new Money(new BigDecimal("100"));

        Balance balance1 = new Balance(money1);
        Balance balance2 = new Balance(money2);

        Account account1 = new Account(1, balance1);
        Account account2 = new Account(2, balance2);

        System.out.println("Initial Balance account1: " + account1.getCurrentBalance().getAmount());
        System.out.println("Initial Balance account2: " + account2.getCurrentBalance().getAmount());

        BalanceTransferService service = new BalanceTransferService();

        int threads = 5;

        Thread[] workers = new Thread[threads];

        Money transferAmount = new Money(new BigDecimal("10"));
        Money transferAmount2 = new Money(new BigDecimal("20"));

        for (int i = 0; i < threads; i++) {

            workers[i] = new Thread(() -> {
                service.transfer(account1, account2, transferAmount);
                service.transfer(account2, account1, transferAmount2);
            });

            workers[i].start();
        }

        // Aguarda todas finalizarem
        for (Thread worker : workers) {
            worker.join();
        }

        System.out.println("Final Balance account1: " + account1.getCurrentBalance().getAmount());
        System.out.println("Final Balance account2: " + account2.getCurrentBalance().getAmount());
    }
}

final class Money {
    private final BigDecimal amount;

    public Money(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Money cannot be negative");
        }
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Money add(Money money) {
        return new Money(this.amount.add(money.amount));
    }

    public Money subtract(Money money) {
        BigDecimal result = this.amount.subtract(money.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Money cannot be negative");
        }
        return new Money(result);
    }
}

final class Balance {
    private Money totalAmount;
    private final Lock lock = new ReentrantLock();

    public Balance(Money money) {
        Objects.requireNonNull(money);
        this.totalAmount = money;
    }

    public Money getTotalAmount() {
        lock.lock();
        try {
            return totalAmount;
        } finally {
            lock.unlock();
        }
    }

    public void deposit(Money depositAmount) {
        lock.lock();
        try {
            this.totalAmount = this.totalAmount.add(depositAmount);
        } finally {
            lock.unlock();
        }
    }

    public void withdraw(Money withdrawAmount) {
        lock.lock();
        try {
            if (!hasEnoughBalance(withdrawAmount)) {
                throw new InsufficientBalanceException("Insufficient balance");
            }
            this.totalAmount = this.totalAmount.subtract(withdrawAmount);
        } finally {
            lock.unlock();
        }
    }

    private boolean hasEnoughBalance(Money money) {
        return totalAmount.getAmount().compareTo(money.getAmount()) >= 0;
    }

    public Lock getLock() {
        return lock;
    }
}

class Account {
    private final int id;
    private final Balance balance;

    public Account(int id, Balance balance) {
        Objects.requireNonNull(balance);
        this.id = id;
        this.balance = balance;
    }

    public int getId() {
        return id;
    }

    public Balance getBalance() {
        return balance;
    }

    public Money getCurrentBalance() {
        return balance.getTotalAmount();
    }
}

class BalanceTransferService {
    public void transfer(Account from, Account to, Money amount) {
        if (from.getId() == to.getId()) {
            throw new IllegalArgumentException("Impossible to transfer to the same account");
        }

        Account first = from;
        Account second = to;

        if (System.identityHashCode(from) > System.identityHashCode(to)) {
            first = to;
            second = from;
        }

        Lock lock1 = first.getBalance().getLock();
        Lock lock2 = second.getBalance().getLock();

        lock1.lock();
        try {
            lock2.lock();
            try {

                System.out.println("[" + Thread.currentThread().getName() + "] " +
                        "Transferring $" + amount.getAmount() +
                        " from account #" + from.getId() +
                        " to account #" + to.getId());

                from.getBalance().withdraw(amount);
                to.getBalance().deposit(amount);

            } finally {
                lock2.unlock();
            }
        } finally {
            lock1.unlock();
        }
    }
}

class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}

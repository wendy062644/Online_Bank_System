package app;

public class Staff {
    private Account account;

    public Staff(int id, String username, String password) {
        this.account = new Account(id, username, password);
    }

    public int getId() {
        return account.getId();
    }

    public String getUsername() {
        return account.getUsername();
    }

    public void viewMemberBalance(Member member) {
        System.out.println("Member " + member.getUsername() + " has balance: " + member.getBalance());
    }

    public void viewTransactionHistory(Member member) {
        System.out.println("Showing transaction history for: " + member.getUsername());
    }

    public boolean checkPassword(String password) {
        return account.checkPassword(password);
    }
}


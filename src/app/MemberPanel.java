package app;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

class MemberPanel implements UserPanel {

    private JFrame frame;

    public MemberPanel() {
        frame = new JFrame("Member Dashboard");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(5, 1, 10, 10));

        JLabel balanceLabel = new JLabel("Balance: $0", JLabel.CENTER);
        frame.add(balanceLabel);

        JButton depositButton = new JButton("Deposit");
        JButton withdrawButton = new JButton("Withdraw");
        JButton transferButton = new JButton("Transfer");
        JButton logoutButton = new JButton("Logout");

        frame.add(depositButton);
        frame.add(withdrawButton);
        frame.add(transferButton);
        frame.add(logoutButton);

        depositButton.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Deposit action"));
        withdrawButton.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Withdraw action"));
        transferButton.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Transfer action"));
        logoutButton.addActionListener(e -> {
            frame.dispose();
            new LoginPage().setVisible(true);
        });
    }

    @Override
    public void showPanel() {
        frame.setVisible(true);
    }
}

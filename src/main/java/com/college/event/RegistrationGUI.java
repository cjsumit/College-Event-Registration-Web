package com.college.event;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegistrationGUI extends JFrame {
    private JTextField studentNameField;
    private JTextField eventNameField;
    private JSpinner ticketsSpinner;
    private JTextField emailField;
    private JTextField phoneField;
    private JButton submitButton;

    public RegistrationGUI() {
        setTitle("College Event Registration");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 320);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Student name
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Student Name:"), gbc);
        studentNameField = new JTextField();
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(studentNameField, gbc);

        // Event name
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Event Name:"), gbc);
        eventNameField = new JTextField();
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(eventNameField, gbc);

        // Tickets
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        panel.add(new JLabel("Tickets:"), gbc);
        ticketsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(ticketsSpinner, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        panel.add(new JLabel("Email (optional):"), gbc);
        emailField = new JTextField();
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(emailField, gbc);

        // Phone
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        panel.add(new JLabel("Phone (optional):"), gbc);
        phoneField = new JTextField();
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(phoneField, gbc);

        // Submit button
        submitButton = new JButton("Register");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(submitButton, gbc);

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSubmit();
            }
        });

        add(panel);
    }

    private void onSubmit() {
        String studentName = studentNameField.getText().trim();
        String eventName = eventNameField.getText().trim();
        int tickets = (Integer) ticketsSpinner.getValue();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (studentName.isEmpty() || eventName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter student name and event name.", "Missing Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean ok = Database.insertRegistration(studentName, eventName, tickets, email.isEmpty() ? null : email, phone.isEmpty() ? null : phone);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Registration saved.\nDB: " + Database.getDbFilePath() + "\nSQL log: " + Database.getSqlLogPath(), "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save registration. Check console for details.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        studentNameField.setText("");
        eventNameField.setText("");
        ticketsSpinner.setValue(1);
        emailField.setText("");
        phoneField.setText("");
    }
}
package bw.bank.model;

public class Customer {
    private final String customerId;
    private final String firstName;
    private final String surname;
    private String address;
    private String phoneNumber;
    private String email;
    private final String username;
    private final String passwordHash;

    public Customer(String customerId, String firstName, String surname, String address,
                    String phoneNumber, String email, String username, String passwordHash) {
        this.customerId = customerId;
        this.firstName = firstName;
        this.surname = surname;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public String getCustomerId() { return customerId; }
    public String getFirstName() { return firstName; }
    public String getSurname() { return surname; }
    public String getFullName() { return firstName + " " + surname; }
    public String getAddress() { return address; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }

    public void updateDetails(String address, String phoneNumber, String email) {
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    @Override
    public String toString() { return customerId + " - " + getFullName(); }
}

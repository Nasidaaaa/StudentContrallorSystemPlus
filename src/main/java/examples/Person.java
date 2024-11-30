package examples;

import java.time.LocalDate;

public class Person extends Date{
    private String name;
    private String gender;
    private String idCardNumber;

    public Person(int year, int month, int day , String name, String gender, String idCardNumber) {
        super(year, month, day);
        this.name = name;
        this.gender = gender;
        this.idCardNumber = idCardNumber;
    }

    public Person(LocalDate date, String name, String gender, String idCardNumber) {
        super();
        this.date = date;
        this.name = name;
        this.gender = gender;
        this.idCardNumber = idCardNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getIdCardNumber() {
        return idCardNumber;
    }

    public void setIdCardNumber(String idCardNumber) {
        this.idCardNumber = idCardNumber;
    }
}
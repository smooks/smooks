package example;

public class Customer {
	
	private String Firstname;
    private String Lastname;
    private Gender Gender;
    private int Age;
	private String Country;
	
    public String getCountry() {
		return Country;
	}
	public void setCountry(String country) {
		Country = country;
	}
    public String getFirstname() {
		return Firstname;
	}
	public void setFirstname(String firstName) {
		Firstname = firstName;
	}
	public String getLastname() {
		return Lastname;
	}
	public void setLastname(String lastName) {
		Lastname = lastName;
	}
	public Gender getGender() {
		return Gender;
	}
	public void setGender(Gender gender) {
		Gender = gender;
	}
	public int getAge() {
		return Age;
	}
	public void setAge(int age) {
		Age = age;
	}

    public String toString() {
        return "[" + Firstname + ", " + Lastname + ", " + Gender + ", " + Age + ", " + Country + "]";
    }
}

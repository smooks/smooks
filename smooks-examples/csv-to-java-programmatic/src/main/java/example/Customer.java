package example;

public class Customer {
	
	private String FirstName;
    private String LastName;
    private Gender Gender;
    private int Age;
	private String Country;
	
    public String getCountry() {
		return Country;
	}
	public void setCountry(String country) {
		Country = country;
	}
    public String getFirstName() {
		return FirstName;
	}
	public void setFirstName(String firstName) {
		FirstName = firstName;
	}
	public String getLastName() {
		return LastName;
	}
	public void setLastName(String lastName) {
		LastName = lastName;
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
        return "[" + FirstName + ", " + LastName + ", " + Gender + ", " + Age + ", " + Country + "]";
    }
}


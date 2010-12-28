package example;

public class Customer {

	private String firstName;
    private String lastName;
    private Gender gender;
    private int age;
	private String country;

    public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
    public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public Gender getGender() {
		return gender;
	}
	public void setGender(Gender gender) {
		this.gender = gender;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}

    public String toString() {
        return "[" + firstName + ", " + lastName + ", " + gender + ", " + age + ", " + country + "]";
    }
}


package se.sj.ipl.rollingstock.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import se.sj.ipl.rollingstock.domain.types.Length;
import se.sj.ipl.rollingstock.domain.types.Speed;
import se.sj.ipl.rollingstock.domain.types.Weight;

public class Vehicle implements Serializable
{
	static final long serialVersionUID = 7491942544148951766L;

	private int id;
	private String vehicleId;
	private String litt;
	private String originalLitt;
	private String ticketId;
	private String vehicleType;
	private Weight weight;
	private Weight dynamicWeight;
	private Weight brakeWeight;
	private Length length;
	private int nrOfAxles;
	private Speed speed;
	private String serviceType;
	private boolean canDoorsBeLocked;
	private List<PhoneNumber> phoneNumbers = new ArrayList<PhoneNumber>() ;
	private PassengerCarConfiguration passengerCarConfiguration;
	private List<Comment> comments = new ArrayList<Comment>() ;
	private String nextAssignment;
	private Schedule schedule;
	private int positionInTrainAssembly;
	private String route;

	public Vehicle() { }

	public Vehicle(String vehicleId, String litt, String originalLitt, String ticketId,
       String vehicleType, Weight weight, Weight dynamicWeight, Weight brakeWeight,
       Length length, int nrOfAxles, Speed speed, String serviceType, boolean canDoorsBeLocked,
       List<PhoneNumber> phoneNumbers, PassengerCarConfiguration passengerCarConfiguration,
       List<Comment> comments, String nextAssignment, Schedule schedule, int positionInTrainAssembly
	   )
	{
		if ( vehicleId == null)
			throw new IllegalArgumentException("vehicleId must not be null");
		if ( litt == null)
			throw new IllegalArgumentException("litt must not be null");
		if ( originalLitt == null)
			throw new IllegalArgumentException("originalLitt must not be null");
		if ( ticketId == null)
			throw new IllegalArgumentException("ticketId must not be null");
		if ( vehicleType == null)
			throw new IllegalArgumentException("vehicleType must not be null");
		if ( weight == null)
			throw new IllegalArgumentException("weight must not be null");
		if ( dynamicWeight == null || !vehicleType.equals("LOK"))
			throw new IllegalArgumentException("dynamicWeight must not be null and vehicleType set to LOK");
		if ( brakeWeight == null)
			throw new IllegalArgumentException("brakeWeight must not be null");
		if ( length == null)
			throw new IllegalArgumentException("length must not be null");
		if ( speed == null)
			throw new IllegalArgumentException("speed must not be null");
		if ( serviceType == null )
			throw new IllegalArgumentException("serviceType must not be null");
		if ( nextAssignment == null)
			throw new IllegalArgumentException("nextAssignment must not be null");
		if ( schedule == null)
			throw new IllegalArgumentException("schedule must not be null");

		this.vehicleId = vehicleId;
		this.litt = litt;
		this.originalLitt = originalLitt;
		this.ticketId = ticketId;
		this.vehicleType = vehicleType;
		this.weight = weight;
		this.dynamicWeight = dynamicWeight;
		this.brakeWeight = brakeWeight;
		this.length = length;
		this.nrOfAxles = nrOfAxles;
		this.speed = speed;
		this.serviceType = serviceType;
		this.canDoorsBeLocked = canDoorsBeLocked;
		this.phoneNumbers = phoneNumbers;
		this.passengerCarConfiguration = passengerCarConfiguration;
		this.comments = comments;
		this.nextAssignment = nextAssignment;
		this.schedule = schedule;
		this.positionInTrainAssembly = positionInTrainAssembly;
	}

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }

	public String getVehicleId() { return vehicleId; }
	public void setVehicleId(String id) { this.vehicleId = id; }

	public String getLitt() { return litt; }
	public void setLitt(String litt) { this.litt = litt; }

	public String getOriginalLitt() { return originalLitt; }
	public void setOriginalLitt(String originalLitt) { this.originalLitt = originalLitt; }

	public String getVehicleType() { return vehicleType; }
	public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

	public int getNrOfAxles() { return nrOfAxles; }
	public void setNrOfAxles(int nrOfAxles) { this.nrOfAxles = nrOfAxles; }

	public String getServiceType() { return serviceType; }
	public void setServiceType(String service) { this.serviceType = service; }

	public int getPositionInTrainAssembly(){return positionInTrainAssembly;}
	public void setPositionInTrainAssembly(int positionInTrainAssembly){this.positionInTrainAssembly = positionInTrainAssembly;}

	public String getTicketId() { return ticketId; }
	public void setTicketId(String ticketId) { this.ticketId = ticketId; }

	public String getNextAssignment(){return nextAssignment;}
	public void setNextAssignment(String nextAssignment){this.nextAssignment = nextAssignment;}

	public boolean isCanDoorsBeLocked() { return canDoorsBeLocked; }
	public void setCanDoorsBeLocked( boolean canDoorsBeLocked)  {  this.canDoorsBeLocked = canDoorsBeLocked;  }

	public String getRoute() { return route; }
	public void setRoute(String route) { this.route = route; }

	public Weight getWeight() { return weight; }
	public void setWeight(Weight weight) { this.weight = weight; }

	public Weight getDynamicWeight() { return dynamicWeight; }
	public void setDynamicWeight(Weight dynamicWeight) { this.dynamicWeight = dynamicWeight; }

	public Weight getDynamicBrakeWeight() { return brakeWeight; }
	public void setDynamicBrakeWeight(Weight dynamicBrakeWeight) { this.brakeWeight = dynamicBrakeWeight; }

	public Speed getSpeed() { return speed; }
	public void setSpeed(Speed speed) { this.speed = speed; }

	public Length getLength() { return length; }
	public void setLength(Length length) { this.length = length; }

	public List<PhoneNumber> getPhoneNumbers() { return phoneNumbers; }
	public void setPhoneNumbers(List<PhoneNumber> phoneNumbers) { this.phoneNumbers = phoneNumbers; }

	public List<Comment> getComments() { return comments; }
	public void setComments(List<Comment> comments) { this.comments = comments; }

	public PassengerCarConfiguration getPassengerCarConfiguration() { return passengerCarConfiguration; }
	public void setPassengerCarConfiguration(PassengerCarConfiguration passenger) { this.passengerCarConfiguration = passenger; }

	public Schedule getSchedule() { return schedule; }
	public void setSchedule(Schedule schedule) { this.schedule = schedule; }

	public int hashCode()
	{
		int hash = 7;
		hash = hash * 31 * ( vehicleId == null  ? 0 : vehicleId.hashCode() ) ;
		hash = hash * 31 * ( litt == null  ? 0 : litt.hashCode() ) ;
		hash = hash * 31 * ( originalLitt == null  ? 0 : originalLitt.hashCode() ) ;
		hash = hash * 31 * ( ticketId == null  ? 0 : ticketId.hashCode() ) ;
		hash = hash * 31 * ( vehicleType == null  ? 0 : vehicleType.hashCode() ) ;
		hash = hash * 31 * ( weight == null  ? 0 : weight.hashCode() ) ;
		hash = hash * 31 * ( dynamicWeight == null  ? 0 : dynamicWeight.hashCode() ) ;
		hash = hash * 31 * ( brakeWeight == null  ? 0 : brakeWeight.hashCode() ) ;
		hash = hash * 31 * nrOfAxles;
		hash = hash * 31 * ( speed == null  ? 0 : speed.hashCode() ) ;
		hash = hash * 31 * ( serviceType == null  ? 0 : serviceType.hashCode() ) ;
		hash = hash * 31 * ( canDoorsBeLocked ? 1 : 0 );
		hash = hash * 31 * ( phoneNumbers == null  ? 0 : phoneNumbers.hashCode() ) ;
		hash = hash * 31 * ( passengerCarConfiguration == null  ? 0 : passengerCarConfiguration.hashCode() ) ;
		hash = hash * 31 * positionInTrainAssembly;
		hash = hash * 31 * ( nextAssignment == null  ? 0 : nextAssignment.hashCode() ) ;
		return hash;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;
		if (obj == null)
			return false;
		if (obj.getClass() != this.getClass())
			return false;

		Vehicle vehicle = ( Vehicle ) obj;
		return	( vehicleId == vehicle.vehicleId ) &&
				( litt != null && litt.equals( vehicle.litt ) &&
				( originalLitt != null && originalLitt.equals( vehicle.originalLitt ) ) &&
				( ticketId != null && ticketId.equals( vehicle.ticketId ) ) ) &&
				( vehicleType != null && vehicleType.equals( vehicle.vehicleType  ) ) &&
				( weight != null && weight.equals( vehicle.weight  ) ) &&
				( dynamicWeight != null && dynamicWeight.equals( vehicle.dynamicWeight )  ) &&
				( brakeWeight != null && brakeWeight.equals( vehicle.dynamicWeight )  ) &&
				( nrOfAxles == vehicle.nrOfAxles ) &&
				( speed != null && speed.equals( vehicle.speed ) ) &&
				( serviceType != null && serviceType.equals( vehicle.serviceType ) ) &&
				( canDoorsBeLocked == vehicle.canDoorsBeLocked ) &&
				( phoneNumbers != null && phoneNumbers.equals( vehicle.phoneNumbers ) ) &&
				( passengerCarConfiguration != null && passengerCarConfiguration.equals( vehicle.passengerCarConfiguration ) ) &&
				( positionInTrainAssembly == vehicle.positionInTrainAssembly ) &&
				( nextAssignment != null && nextAssignment.equals( vehicle.nextAssignment ) ) ;
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append( "[vehicle:" );
		sb.append( ", vehicleId=" ).append( vehicleId );
		sb.append( ", litt=" ).append( litt );
		sb.append( ", originalLitt=" ).append( originalLitt );
		sb.append( ", tickedId=" ).append( ticketId );
		sb.append( ", vehicleType=" ).append( vehicleType );
		sb.append( ", weight=" ).append( weight );
		sb.append( ", dynamicWeight=" ).append( dynamicWeight );
		sb.append( ", brakeWeight=" ).append( brakeWeight );
		sb.append( ", nrOfAxles=" ).append( nrOfAxles );
		sb.append( ", speed=" ).append( speed );
		sb.append( ", length=" ).append( length );
		sb.append( ", serviceTyp=" ).append( serviceType );
		sb.append( ", canDoorsBeLocked=" ).append( canDoorsBeLocked );
		sb.append( ", phoneNumbers=" ).append( phoneNumbers );
		sb.append( ", comments=" ).append( comments );
		sb.append( ", passengerCarConfiguration=" ).append( passengerCarConfiguration );
		sb.append( ", posistionInTrainAssembly=" ).append( positionInTrainAssembly );
		sb.append( ", nextAssignment=" ).append( nextAssignment );
		sb.append( ", schedule=" ).append( schedule );
		sb.append("]").append(vehicleId);
		return sb.toString();
	}
}

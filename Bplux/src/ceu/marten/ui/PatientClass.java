package ceu.marten.ui;

import java.io.Serializable;

import android.content.Context;

public class PatientClass implements Serializable{
	
	private static final long serialVersionUID = -4487071327586521666L;
	
	private static Context context;
	public String patient_name;
	public String health_number;
	public boolean gender;//true = male , false = female
	public String birth_year;
	public String birth_month;
	public String birth_day;
	
	public PatientClass() {
	}
	
	public PatientClass(Context _context) {
		PatientClass.context = _context;
	}
	
	public void setPatientName(String patient_name) {
		this.patient_name = patient_name;
	}
	
	public String getPatientName() {
		return patient_name;
	}
	
	public void setHealthNumber(String health_number) {
		this.health_number = health_number;
	}
	
	public String getHealthNumber(){
		return health_number;
	}
	
	public void setGender(boolean gender) {
		this.gender = gender;
	}
	
	public boolean getGender() {
		return gender;
	}
	
	public void setBirthYear(String birth_year) {
		this.birth_year = birth_year;
	}
	
	public String getBirthYear(){
		return birth_year;
	}
	
	public void setBirthMonth(String birth_month) {
		this.birth_month = birth_month;
	}
	
	public String getBirthMonth(){
		return birth_month;
	}
	
	public void setBirthDay(String birth_day) {
		this.birth_day = birth_day;
	}
	
	public String getBirthDay(){
		return birth_day;
	}
	
}

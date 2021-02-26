package io.hapifhir.playground;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import ca.uhn.fhir.util.BundleUtil;

public class SampleClient {

	public static void main(String[] args) {

		StringBuffer sb = new StringBuffer();
		List<Patient> patientsresp = new ArrayList<Patient>();
		getPatientinfo("SMITH", sb, patientsresp );		
		patientsresp.stream().sorted((o1,o2)->o1.getNameFirstRep().getFamily().compareTo(o2.getNameFirstRep().getFamily())).forEach(s->System.out.println("  last name  "+s.getNameFirstRep().getFamily()+" first name "+s.getNameFirstRep().getGiven()+ "   Birth Date   " + s.getBirthDate()));

		String fileName = "src/test/resources/lines.txt";
		List<String> list = new ArrayList<String>();
		try (BufferedReader br = Files.newBufferedReader(Paths.get(fileName))) {
			// br returns as stream and convert it into a List
			list = br.lines().collect(Collectors.toList());

		} catch (IOException e) {
			e.printStackTrace();
		}
		long startTime = System.nanoTime();
		List<Patient> patientsresparray = new ArrayList<Patient>();
		for (String s : list) {			
			getPatientinfo(s, sb, patientsresparray);
		}
		long estimatedTime = System.nanoTime() - startTime;
		System.out.println("  Time taken to get response" + estimatedTime);
		System.out.println(sb);		
		patientsresparray.stream().sorted((o1,o2)->o1.getNameFirstRep().getFamily().compareTo(o2.getNameFirstRep().getFamily())).forEach(s->System.out.println("  last name  "+s.getNameFirstRep().getFamily()+" first name "+s.getNameFirstRep().getGiven()+ "   Birth Date   " + s.getBirthDate()));


	}

	public static void getPatientinfo(String name, StringBuffer sb , List<Patient> patientsresp ) {
		List<IBaseResource> patients = new ArrayList<>();
		// Create a FHIR client
		FhirContext fhirContext = FhirContext.forR4();
		IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
		client.registerInterceptor(new LoggingInterceptor(false));
		// Search for Patient resources
		Bundle response = client.search().forResource("Patient").where(Patient.FAMILY.matches().value(name))
				.returnBundle(Bundle.class).execute();
		patients.addAll(BundleUtil.toListOfResources(fhirContext, response));
		
		for (IBaseResource ibaseResource : patients) {
			Patient p = (Patient) ibaseResource;			
			patientsresp.add(p);
			List<HumanName> humanName = p.getName();
			for (HumanName h : humanName) {				
				sb.append(" Birth Date " + p.getBirthDate() + " Last Name " + h.getFamily() + " First Name"
						+ h.getGiven());
			}
		}		
	}
}

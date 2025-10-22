package com.school.equipmentlending;

import com.school.equipmentlending.model.Equipment;
import com.school.equipmentlending.repository.EquipmentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EquipmentLendingBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(EquipmentLendingBackendApplication.class, args);

	}
	/*@Bean
	CommandLineRunner run(EquipmentRepository repo) {
		return args -> {
			repo.save(new Equipment(null, "Projector", "Electronics", true));
			repo.save(new Equipment(null, "Microscope", "Science Lab", false));

			System.out.println(repo.findAll());
		};
	}*/
}

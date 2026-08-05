package com.penpals.users;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "app_user")
@Inheritance(strategy = InheritanceType.JOINED)
public class AppUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	private String authId;

	@Column(nullable = false)
	private String firstName;

	private String lastName;

	private String email;

	private String phone;

	private String whatsapp;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private RoleEnum role;

}
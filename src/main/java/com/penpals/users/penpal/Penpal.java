package com.penpals.users.penpal;

import com.penpals.common.State;
import com.penpals.users.AppUser;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "penpal")
public class Penpal extends AppUser {

	private Integer age;

	@Enumerated(EnumType.STRING)
	private State state;

	private String biography;

	@ManyToOne
	private AppUser parentHelper;

}
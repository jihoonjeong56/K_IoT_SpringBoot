package com.example.k5_iot_springboot.entity;

import com.example.k5_iot_springboot.common.enums.RoleType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Getter
@NoArgsConstructor
public class G_Role {
    @Id @Enumerated(EnumType.STRING)
    @Column(name = "role_name", length = 30, nullable = false)
    private RoleType name;

    public G_Role(RoleType name){
        this.name = name;
    }
}

package com.example.k5_iot_springboot.entity;

import com.example.k5_iot_springboot.entity.base.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;


@Entity
@Table(name = "projects",
        indexes = {
                @Index(name = "idx_project_name", columnList = "name"),
                @Index(name = "idx_project_createAt", columnList = "created_at")
        }, uniqueConstraints = {@UniqueConstraint(name = "uk_projects_name", columnNames = "name")})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter


public class Project extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_project_user_id"))
    private G_User user;

    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    private String name;
//
//    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Task> tasks = new ArrayList<>();
//
//    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Tag> tags = new ArrayList<>();


    public Project(String name, G_User user) {
        this.name = name;
        this.user = user;
    }
    public static Project create(String name, G_User user){
        return new Project(name, user);
    }
    public void update(String name){
        this.name = name;
    }
}

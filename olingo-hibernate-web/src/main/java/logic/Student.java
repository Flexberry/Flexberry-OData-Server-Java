package logic;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="Student")
public class Student {
    
    private Long id;    
    private String name;    
    private Long age;
    
    public Student(){
        name = null;
    }
    
    public Student(Student s){
        name = s.getName();
    }   
    
	@Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy = "increment")
    @Column(name="id")
    public Long getId() {
        return id;
    }
    
	@Column(name="name")
    public String getName(){
        return name;
    }
    
	@Column(name="age")
    public Long getAge(){
        return age;
    }
    
    public void setId(Long i){
        id = i;     
    }
    
    public void setName(String s){
        name = s;
    }   
    
    public void setAge(Long l){
        age = l;
    }   
}
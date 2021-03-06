package servicebus;
// Generated 21.04.2015 10:12:53 by Hibernate Tools 4.3.1


import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Тэг generated by hbm2java
 */
@Entity
@Table(name="Тэг"
    ,catalog="ServiceBus"
)
public class Тэг  implements java.io.Serializable {


     private String primaryKey;
     private Сообщение сообщение;
     private String имя;
     private String значение;
     private Date createTime;
     private String creator;
     private Date editTime;
     private String editor;

    public Тэг() {
    }

	
    public Тэг(String primaryKey, Сообщение сообщение) {
        this.primaryKey = primaryKey;
        this.сообщение = сообщение;
    }
    public Тэг(String primaryKey, Сообщение сообщение, String имя, String значение, Date createTime,
        String creator, Date editTime, String editor) {
       this.primaryKey = primaryKey;
       this.сообщение = сообщение;
       this.имя = имя;
       this.значение = значение;
       this.createTime = createTime;
       this.creator = creator;
       this.editTime = editTime;
       this.editor = editor;
    }
   
     @Id 

    
    @Column(name="primaryKey", unique=true, nullable=false, length=36)
    public String getPrimaryKey() {
        return this.primaryKey;
    }
    
    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="Сообщение_m0", nullable=false)
    public Сообщение getСообщение() {
        return this.сообщение;
    }
    
    public void setСообщение(Сообщение сообщение) {
        this.сообщение = сообщение;
    }

    
    @Column(name="Имя")
    public String getИмя() {
        return this.имя;
    }
    
    public void setИмя(String имя) {
        this.имя = имя;
    }

    
    @Column(name="Значение")
    public String getЗначение() {
        return this.значение;
    }
    
    public void setЗначение(String значение) {
        this.значение = значение;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="CreateTime", length=23)
    public Date getCreateTime() {
        return this.createTime;
    }
    
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    
    @Column(name="Creator")
    public String getCreator() {
        return this.creator;
    }
    
    public void setCreator(String creator) {
        this.creator = creator;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="EditTime", length=23)
    public Date getEditTime() {
        return this.editTime;
    }
    
    public void setEditTime(Date editTime) {
        this.editTime = editTime;
    }

    
    @Column(name="Editor")
    public String getEditor() {
        return this.editor;
    }
    
    public void setEditor(String editor) {
        this.editor = editor;
    }




}



package servicebus;

// Generated 21.04.2015 10:12:53 by Hibernate Tools 4.3.1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Stormsettings generated by hbm2java
 */
@Entity
@Table(name = "STORMSETTINGS", catalog = "ServiceBus")
public class Stormsettings implements java.io.Serializable {

	private String primaryKey;
	private String module;
	private String name;
	private String value;
	private String user;

	public Stormsettings() {
	}

	public Stormsettings(String primaryKey) {
		this.primaryKey = primaryKey;
	}

	public Stormsettings(String primaryKey, String module, String name,
			String value, String user) {
		this.primaryKey = primaryKey;
		this.module = module;
		this.name = name;
		this.value = value;
		this.user = user;
	}

	@Id
	@Column(name = "primaryKey", unique = true, nullable = false, length = 36)
	public String getPrimaryKey() {
		return this.primaryKey;
	}

	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}

	@Column(name = "Module", length = 1000)
	public String getModule() {
		return this.module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	@Column(name = "Name")
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "Value")
	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Column(name = "`User`")
	public String getUser() {
		return this.user;
	}

	public void setUser(String user) {
		this.user = user;
	}

}
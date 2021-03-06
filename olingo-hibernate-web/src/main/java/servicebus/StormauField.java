package servicebus;

// Generated 21.04.2015 10:12:53 by Hibernate Tools 4.3.1

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * StormauField generated by hbm2java
 */
@Entity
@Table(name = "STORMAuField", catalog = "ServiceBus")
public class StormauField implements java.io.Serializable {

	private String primaryKey;
	private StormauEntity stormauEntity;
	private StormauField stormauField;
	private Serializable field;
	private Serializable oldValue;
	private Serializable newValue;
	private Set<StormauField> stormauFields = new HashSet<StormauField>(0);

	public StormauField() {
	}

	public StormauField(String primaryKey, StormauEntity stormauEntity,
			Serializable field) {
		this.primaryKey = primaryKey;
		this.stormauEntity = stormauEntity;
		this.field = field;
	}

	public StormauField(String primaryKey, StormauEntity stormauEntity,
			StormauField stormauField, Serializable field,
			Serializable oldValue, Serializable newValue,
			Set<StormauField> stormauFields) {
		this.primaryKey = primaryKey;
		this.stormauEntity = stormauEntity;
		this.stormauField = stormauField;
		this.field = field;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.stormauFields = stormauFields;
	}

	@Id
	@Column(name = "primaryKey", unique = true, nullable = false, length = 36)
	public String getPrimaryKey() {
		return this.primaryKey;
	}

	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "AuditEntity_m0", nullable = false)
	public StormauEntity getStormauEntity() {
		return this.stormauEntity;
	}

	public void setStormauEntity(StormauEntity stormauEntity) {
		this.stormauEntity = stormauEntity;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MainChange_m0")
	public StormauField getStormauField() {
		return this.stormauField;
	}

	public void setStormauField(StormauField stormauField) {
		this.stormauField = stormauField;
	}

	@Column(name = "Field", nullable = false)
	public Serializable getField() {
		return this.field;
	}

	public void setField(Serializable field) {
		this.field = field;
	}

	@Column(name = "OldValue")
	public Serializable getOldValue() {
		return this.oldValue;
	}

	public void setOldValue(Serializable oldValue) {
		this.oldValue = oldValue;
	}

	@Column(name = "NewValue")
	public Serializable getNewValue() {
		return this.newValue;
	}

	public void setNewValue(Serializable newValue) {
		this.newValue = newValue;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "stormauField")
	public Set<StormauField> getStormauFields() {
		return this.stormauFields;
	}

	public void setStormauFields(Set<StormauField> stormauFields) {
		this.stormauFields = stormauFields;
	}

}

package servicebus;

// Generated 21.04.2015 10:12:53 by Hibernate Tools 4.3.1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Stormwebsearch generated by hbm2java
 */
@Entity
@Table(name = "STORMWEBSEARCH", catalog = "ServiceBus")
public class Stormwebsearch implements java.io.Serializable {

	private String primaryKey;
	private Stormfiltersetting stormfiltersetting;
	private String name;
	private int order;
	private String presentView;
	private String detailedView;

	public Stormwebsearch() {
	}

	public Stormwebsearch(String primaryKey,
			Stormfiltersetting stormfiltersetting, String name, int order,
			String presentView, String detailedView) {
		this.primaryKey = primaryKey;
		this.stormfiltersetting = stormfiltersetting;
		this.name = name;
		this.order = order;
		this.presentView = presentView;
		this.detailedView = detailedView;
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
	@JoinColumn(name = "FilterSetting_m0", nullable = false)
	public Stormfiltersetting getStormfiltersetting() {
		return this.stormfiltersetting;
	}

	public void setStormfiltersetting(Stormfiltersetting stormfiltersetting) {
		this.stormfiltersetting = stormfiltersetting;
	}

	@Column(name = "Name", nullable = false)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "`Order`", nullable = false)
	public int getOrder() {
		return this.order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Column(name = "PresentView", nullable = false)
	public String getPresentView() {
		return this.presentView;
	}

	public void setPresentView(String presentView) {
		this.presentView = presentView;
	}

	@Column(name = "DetailedView", nullable = false)
	public String getDetailedView() {
		return this.detailedView;
	}

	public void setDetailedView(String detailedView) {
		this.detailedView = detailedView;
	}

}

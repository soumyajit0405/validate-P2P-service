package com.energytrade.app.model;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;


/**
 * The persistent class for the state_board_mappings database table.
 * 
 */
@Entity
@Table(name="state_board_mappings")
@NamedQuery(name="StateBoardMapping.findAll", query="SELECT s FROM StateBoardMapping s")
public class StateBoardMapping implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="state_board_mapping_id")
	private int stateBoardMappingId;

	@Column(name="active_status")
	private byte activeStatus;

	@Column(name="created_by")
	private String createdBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="created_ts")
	private Date createdTs;

	private byte softdeleteflag;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="sync_ts")
	private Date syncTs;

	@Column(name="updated_by")
	private String updatedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="updated_ts")
	private Date updatedTs;

	//bi-directional many-to-one association to AllState
	@ManyToOne
	@JoinColumn(name="state_id")
	private AllState allState;

	//bi-directional many-to-one association to AllElectricityBoard
	@ManyToOne
	@JoinColumn(name="electricity_board_id")
	private AllElectricityBoard allElectricityBoard;

	public StateBoardMapping() {
	}

	public int getStateBoardMappingId() {
		return this.stateBoardMappingId;
	}

	public void setStateBoardMappingId(int stateBoardMappingId) {
		this.stateBoardMappingId = stateBoardMappingId;
	}

	public byte getActiveStatus() {
		return this.activeStatus;
	}

	public void setActiveStatus(byte activeStatus) {
		this.activeStatus = activeStatus;
	}

	public String getCreatedBy() {
		return this.createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedTs() {
		return this.createdTs;
	}

	public void setCreatedTs(Date createdTs) {
		this.createdTs = createdTs;
	}

	public byte getSoftdeleteflag() {
		return this.softdeleteflag;
	}

	public void setSoftdeleteflag(byte softdeleteflag) {
		this.softdeleteflag = softdeleteflag;
	}

	public Date getSyncTs() {
		return this.syncTs;
	}

	public void setSyncTs(Date syncTs) {
		this.syncTs = syncTs;
	}

	public String getUpdatedBy() {
		return this.updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public Date getUpdatedTs() {
		return this.updatedTs;
	}

	public void setUpdatedTs(Date updatedTs) {
		this.updatedTs = updatedTs;
	}

	public AllState getAllState() {
		return this.allState;
	}

	public void setAllState(AllState allState) {
		this.allState = allState;
	}

	public AllElectricityBoard getAllElectricityBoard() {
		return this.allElectricityBoard;
	}

	public void setAllElectricityBoard(AllElectricityBoard allElectricityBoard) {
		this.allElectricityBoard = allElectricityBoard;
	}

}
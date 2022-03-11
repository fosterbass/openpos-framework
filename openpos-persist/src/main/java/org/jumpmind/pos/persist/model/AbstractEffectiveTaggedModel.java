package org.jumpmind.pos.persist.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jumpmind.pos.persist.ColumnDef;

@NoArgsConstructor
abstract public class AbstractEffectiveTaggedModel extends AbstractTaggedModel implements IEffectiveDateModel {

    @SuppressWarnings("unused")
    private static final long serialVersionUID = 1L;

    @ColumnDef
    Date effectiveStartTime;
    
    @ColumnDef 
    Date effectiveEndTime;

    public AbstractEffectiveTaggedModel(Date effectiveStartTime, Date effectiveEndTime) {
        if (effectiveStartTime != null) {
            this.effectiveStartTime = effectiveStartTime;
        } else {
            this.effectiveStartTime = new Date();
        }
        this.effectiveEndTime = effectiveEndTime;
    }

    @Override
    public void setEffectiveStartTime(Date effectiveStartTime) {
        this.effectiveStartTime = effectiveStartTime;
    }
    
    @Override
    public Date getEffectiveStartTime() {
        return effectiveStartTime;
    }
    
    @Override
    public void setEffectiveEndTime(Date effectiveEndTime) {
        this.effectiveEndTime = effectiveEndTime;
    }
    
    @Override
    public Date getEffectiveEndTime() {
        return effectiveEndTime;
    }

    public void cloneAbstractEffectiveTaggedModelFields(AbstractEffectiveTaggedModel model) {
        model.setEffectiveEndTime(this.getEffectiveEndTime());
        model.setEffectiveStartTime(this.getEffectiveStartTime());

        cloneAbstractTaggedModelField(model);
    }
}

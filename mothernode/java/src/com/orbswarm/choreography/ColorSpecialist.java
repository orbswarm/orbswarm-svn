package com.orbswarm.choreography;

import com.orbswarm.swarmcomposer.color.HSV;

public interface ColorSpecialist extends Specialist {
    public void setColor(HSV color, float fadeTimeSec);
}

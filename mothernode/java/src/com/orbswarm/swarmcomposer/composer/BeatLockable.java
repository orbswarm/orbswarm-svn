package com.orbswarm.swarmcomposer.composer;



/**
 * @author Simran Gleason
 */
public class BeatLockable {
    protected int beatLock = 0;
    protected int bpm = 0;
    protected long beatLengthMS = 0;
    protected long measureLength = 0;
    protected boolean beatLockSpecified = false;
    protected boolean bpmSpecified = false;

    public int getBPM() {
        return this.bpm;
    }
    
    public void setBPM(int val) {
        this.bpm = val;
        bpmSpecified = true;
        recalculateBeatLock();
    }

    public void setBeatLock(int val) {
        this.beatLock = val;
        beatLockSpecified = true;
        recalculateBeatLock();
    }

    public int getBeatLock() {
        return this.beatLock;
    }

    public void recalculateBeatLock() {
        // ms/beat = (60s/min) / (bpm beats/min) * (1000 ms/s)
        this.beatLengthMS = 60000 / getBPM();
        this.measureLength = this.getBeatLock() * beatLengthMS;
    }

    public long getBeatLengthMS() {
        return beatLengthMS;
    }

    public long getMeasureLength() {
        return measureLength;
    }

    // can fudge a bit & lead or lag the beat.
    // (& somehow that will be tweakable sometime in the fyoocher)
    int fudgeFactor_lead = 10;
    int fudgeFactor_lag = 5;
    public long getWaitForBeatTime(long songPlayTime) {
        if (beatLock == 0) {
            return 0;
        }
        long measureRemainder = songPlayTime % this.getMeasureLength();
        if (measureRemainder < fudgeFactor_lag) {
            return 0;
        } else {
            long timeToNextMeasure = measureLength - measureRemainder;
            if (timeToNextMeasure < fudgeFactor_lead) {
                return 0;
            }
            return timeToNextMeasure;
        }
    }
    
    

}
    
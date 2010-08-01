package com.orbswarm.swarmcon.model;

import static java.lang.Math.min;
import static java.lang.Math.max;

import java.util.concurrent.atomic.AtomicReference;

// a rate

public class Rate
{
  final private String mName;
  final private AtomicReference<Double> mRate;
  final private AtomicReference<Double> mTarget;
  final private double mMin;
  final private double mMax;
  final private double mAcceleration;

  public Rate(String name, double min, double max, double acceleration)
  {
    mName = name;
    mMin = min;
    mMax = max;
    mAcceleration = acceleration;
    mRate = new AtomicReference<Double>(new Double(0));
    mTarget = new AtomicReference<Double>(new Double(0));
  }

  // clone this rate

  public Rate clone()
  {
    Rate other = new Rate(mName, mMin, mMax, mAcceleration);
    other.setRate(getRate());
    other.setTarget(getTarget());
    return other;
  }

  // get maximum rate

  public double getMax()
  {
    return mMax;
  }

  // get minimum rate

  public double getMin()
  {
    return mMin;
  }

  // stipulate the rate

  public void setRate(double rate)
  {
    mRate.set(max(mMin, min(rate, mMax)));
  }

  // get current rate

  public double getRate()
  {
    return mRate.get();
  }

  // set target rate

  public void setTarget(double target)
  {
    mTarget.set(target);
  }

  // set target as normalized value from -1 to 1

  public void setNormalizedTarget(double target)
  {
    assert (target >= -1 && target <= 1);
    setTarget(mMin + (mMax - mMin) * ((target + 1) / 2));
  }

  // get target

  public double getTarget()
  {
    return mTarget.get();
  }

  // get target as a normalized value from 0 to 1

  public double getNormalizedTarget()
  {
    return ((mTarget.get() - mMin) / (mMax - mMin)) * 2 - 1;
  }

  // update the rate

  public double update(double time)
  {
    if (mTarget.get() > mRate.get())
      mRate.set(min(mRate.get() + mAcceleration * time, mTarget.get()));
    else if (mTarget.get() < mRate.get())
      mRate.set(max(mRate.get() - mAcceleration * time, mTarget.get()));

    return mRate.get();
  }
}

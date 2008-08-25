package com.orbswarm.swarmcon;

import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Map;
import java.util.Map.Entry;

/** The orb message class. */

public class Message extends Properties
{
    /** Known message types. */

    enum Type
    {
      POSITION_REPORT("p"), 
        SURVEY_REPORT("s"), 
        INFO_REPORT("i"), 
        WAYPOINT_ACK("w"),
        ORIGIN_ACK("o"),
        UNKNOWN("");

      private String token = null;

      Type(String token)
      {
        this.token = token;
      }

      public static Type getType(String token)
      {
        for (Type type: Type.values())
          if (type.token.equalsIgnoreCase(token))
            return type;

        return UNKNOWN;
      }

      public String toString()
      {
        return super.toString();
      }
    };

    /** Known message fields. */

    enum Field
    {
      EASTING("e"), NORTHING("n"), YAW("y"), UNKNOWN("");

      private String token = null;

      Field(String token)
      {
        this.token = token;
      }

      public static Field getField(String token)
      {
        for (Field field: Field.values())
          if (field.token.equalsIgnoreCase(token))
            return field;

        return UNKNOWN;
      }

      public String toString()
      {
        return token;
      }
    };

    /** The message type. */

    private Type type;

    /** The idenifier of the sender of the messsage. */

    private int senderId;

    /** Construct a message from given message text. 
     * 
     * @param message the message text
     */

    public Message(String message)
    {
      try
      {
        parseMessage(message);
      }
      catch (Exception e)
      {
        // do nothing for now
      }
    }
        
    /** Get the message type.
     *
     * @return the message type.
     */

    public Type getType()
    {
      return type;
    }

    /** Get the id of message sender.  For orbs that currently an number
     * between 60 and 65.
     *
     * @return the message senderId.
     */

    public int getSenderId()
    {
      return senderId;
    }

    // parser in a message 

    private void parseMessage(String message) 
    {
      StringTokenizer t = new StringTokenizer(message, " @\t\n\r\f={}");
      
      // assume the type is unknown

        type = Type.UNKNOWN;
      
      // if there are not tokens it's an empty message, we're done

      if (!t.hasMoreTokens())
        return;

      // get the sender id

      if (!t.hasMoreTokens())
        return;
      senderId = Integer.valueOf(t.nextToken()) - SwarmCon.ORB_OFFSET_ID;

      // get the message type

      if (!t.hasMoreTokens())
        return;
      type = Type.getType(t.nextToken());

      // loop through the remaining tokens and parse them in as
      // properties

      while (t.hasMoreTokens())
      {
        // get the property

        String key = t.nextToken();

        // get the value
 
        if (!t.hasMoreTokens())
        {
          type = Type.UNKNOWN;
          return;
        }
        String value = t.nextToken();
 
        // set property value
 
        setProperty(key, value);
      }
    }

    /** {@inheritDoc} */
        
    @Override
    public String toString()
    {
      StringBuffer buf = new StringBuffer(type + "[orb=" + senderId);
          
      for (Entry<Object,Object> entry: entrySet())
        buf.append(" " + entry.getKey() + "=" + entry.getValue());
      
      buf.append("]");
      return buf.toString();
    }

    /** Get string property.
     *
     * @param name the name of the property
     *
     * @return the string value of the property
     */ 

    public String getStringProperty(String name)
    {
      return (String)getProperty(name);
    }

    /** Get double property.
     *
     * @param name the name of the property
     *
     * @return the double value of the property
     */ 

    public Double getDoubleProperty(String name)
    {
      double value = 0;

      try
      {
        value = Double.valueOf((String)getProperty(name));
      }
      catch (Exception e)
      {
      }

      return value;
    }

    /** Get double property.
     *
     * @param field the name of the property
     *
     * @return the double value of the property
     */ 

    public Double getDoubleProperty(Field field)
    {
      return getDoubleProperty(field.toString());
    }
}

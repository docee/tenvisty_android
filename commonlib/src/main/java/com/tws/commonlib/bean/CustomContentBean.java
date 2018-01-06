package com.tws.commonlib.bean;

/**
 * Created by Administrator on 2017/7/29.
 */

import java.io.Serializable;

public class CustomContentBean
        implements Serializable
{
    private String endHour;
    private String endMin;
    private int open;
    private int selectedFive = 0;
    private int selectedFour = 0;
    private int selectedOne = 0;
    private int selectedSeventh = 0;
    private int selectedSix = 0;
    private int selectedTwo = 0;
    private int selectedthree = 0;
    private int show;
    private String startHour;
    private String startMin;

    public String getEndHour()
    {
        return this.endHour;
    }

    public String getEndMin()
    {
        return this.endMin;
    }

    public int getOpen()
    {
        return this.open;
    }

    public int getShow()
    {
        return this.show;
    }

    public String getStartHour()
    {
        return this.startHour;
    }

    public String getStartMin()
    {
        return this.startMin;
    }

    public int isSelectedFive()
    {
        return this.selectedFive;
    }

    public int isSelectedFour()
    {
        return this.selectedFour;
    }

    public int isSelectedOne()
    {
        return this.selectedOne;
    }

    public int isSelectedSeventh()
    {
        return this.selectedSeventh;
    }

    public int isSelectedSix()
    {
        return this.selectedSix;
    }

    public int isSelectedTwo()
    {
        return this.selectedTwo;
    }

    public int isSelectedthree()
    {
        return this.selectedthree;
    }

    public void setEndHour(String paramString)
    {
        this.endHour = paramString;
    }

    public void setEndMin(String paramString)
    {
        this.endMin = paramString;
    }

    public void setOpen(int paramInt)
    {
        this.open = paramInt;
    }

    public void setSelectedFive(int paramInt)
    {
        this.selectedFive = paramInt;
    }

    public void setSelectedFour(int paramInt)
    {
        this.selectedFour = paramInt;
    }

    public void setSelectedOne(int paramInt)
    {
        this.selectedOne = paramInt;
    }

    public void setSelectedSeventh(int paramInt)
    {
        this.selectedSeventh = paramInt;
    }

    public void setSelectedSix(int paramInt)
    {
        this.selectedSix = paramInt;
    }

    public void setSelectedTwo(int paramInt)
    {
        this.selectedTwo = paramInt;
    }

    public void setSelectedthree(int paramInt)
    {
        this.selectedthree = paramInt;
    }

    public void setShow(int paramInt)
    {
        this.show = paramInt;
    }

    public void setStartHour(String paramString)
    {
        this.startHour = paramString;
    }

    public void setStartMin(String paramString)
    {
        this.startMin = paramString;
    }
}
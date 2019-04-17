package com.holtek.libHTBodyfat;

import java.util.Hashtable;

public class HTPeopleGeneral {

    public Hashtable<String, String> bmiRange;
    public double[] bmiValues = new double[] {};
    public Hashtable<String, String> standardCost;
    public double[] standardCostValues = new double[] {};
    public Hashtable<String, String> internalOrganFatRange;
    public double[] interalOrganFatValues = new double[] {};
    public Hashtable<String, String> boneRange;
    public double[] boneValues = new double[] {};
    public Hashtable<String, String> bodyFatRange;
    public double[] bodyFatValues = new double[] {};
    public Hashtable<String, String> waterRange;
    public double[] waterValues = new double[] {};
    public Hashtable<String, String> muscleRange;
    public double[] muscleValues = new double[] {};

    public double weight;
    public double height;
    int gender;
    int age;
    int impdeance;

    public HTPeopleGeneral(double weight, double height, int gender, int age, int impdeance) {
        this.weight = weight;
        this.height = height;
        this.gender = gender;
        this.age = age;
        this.impdeance = impdeance;

        this.bmiRange = new Hashtable<>();
        this.standardCost = new Hashtable<>();
        this.internalOrganFatRange = new Hashtable<>();
        this.boneRange = new Hashtable<>();
        this.bodyFatRange = new Hashtable<>();
        this.waterRange = new Hashtable<>();
        this.muscleRange = new Hashtable<>();
    }

    public double e;
    public double f;
    public double bmi;
    public int baseCost;
    public int internalOrganFat;
    public double bone;
    public double bodyFat;
    public double water;
    public double muscle;

    public int calculate() {
        int ret = HTBodyfat.NN(this.weight, this.height, this.age, this.gender, this.impdeance);
        if (ret != 0) {
            if (ret != 3 || ret != 4) {
                this.bmi = 0;
            } else {
                this.bmi = HTBodyfat.EE();
                this.f = HTBodyfat.QQ();
            }
            this.internalOrganFat = 0;
            this.bodyFat = 0;
            this.water = 0;
            this.muscle = 0;
            this.bone = 0;
            this.baseCost = 0;
            this.e = HTBodyfat.AA();
        } else {
            this.bodyFat = HTBodyfat.CC();
            this.water = HTBodyfat.HH();
            this.bone = HTBodyfat.DD();
            this.muscle = HTBodyfat.FF();
            this.internalOrganFat = (int)HTBodyfat.GG();
            this.baseCost = (int)HTBodyfat.BB();
            this.bmi = HTBodyfat.EE();
            this.f = HTBodyfat.QQ();
            {
                double[] value = HTBodyfat.II();
                this.bmiRange.put("瘦－普通", String.valueOf(value[0]));
                this.bmiRange.put("普通－偏胖", String.valueOf(value[1]));
                this.bmiRange.put("偏胖－肥胖", String.valueOf(value[2]));
                this.bmiValues = value;
            }
            {
                double[] value = HTBodyfat.JJ();
                this.standardCost.put("偏低－达标", String.valueOf(value[0]));
                this.standardCostValues = value;
            }
            {
                double[] value = HTBodyfat.KK();
                this.bodyFatRange.put("瘦－标准－", String.valueOf(value[0]));
                this.bodyFatRange.put("标准－－标准＋", String.valueOf(value[1]));
                this.bodyFatRange.put("标准＋－偏胖", String.valueOf(value[2]));
                this.bodyFatRange.put("偏胖－肥胖", String.valueOf(value[3]));
                this.bodyFatValues = value;
            }
            {
                double[] value = HTBodyfat.LL();
                this.boneRange.put("不足－标准", String.valueOf(value[0]));
                this.boneRange.put("标准－优秀", String.valueOf(value[1]));
                this.boneValues = value;
            }
            {
                double[] value = HTBodyfat.OO();
                this.internalOrganFatRange.put("不足－标准", String.valueOf(value[0]));
                this.internalOrganFatRange.put("标准－优秀", String.valueOf(value[1]));
                this.interalOrganFatValues = value;
            }
            {
                double[] value = HTBodyfat.PP();
                this.waterRange.put("不足－标准", String.valueOf(value[0]));
                this.waterRange.put("标准－优秀", String.valueOf(value[1]));
                this.waterValues = value;
            }
            {
                double[] value = HTBodyfat.MM();
                this.muscleRange.put("不足－标准", String.valueOf(value[0]));
                this.muscleRange.put("标准－优秀", String.valueOf(value[1]));
                this.muscleValues = value;
            }
            this.e = HTBodyfat.AA();
        }
        return ret;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("==============>>>>>>>>Body Fat Start<<<<<<<<============\n");
        sb.append(String.format("weight:  %f\n", this.weight));
        sb.append(String.format("height:  %f\n", this.height));
        sb.append(String.format("age:  %d\n", this.age));
        sb.append(String.format("gender:  %d\n", this.gender));
        sb.append(String.format("impdeance:  %d\n", this.impdeance));
        sb.append(String.format("e:  %f\n", this.e));
        sb.append(String.format("f:  %f\n", this.f));
        sb.append(String.format("bmi:  %f\n", this.bmi));
        sb.append(String.format("baseCost:  %d\n", this.baseCost));
        sb.append(String.format("internalOrganFat:  %d\n", this.internalOrganFat));
        sb.append(String.format("bone:  %f\n", this.bone));
        sb.append(String.format("bodyFat:  %f\n", this.bodyFat));
        sb.append(String.format("water:  %f\n", this.water));
        sb.append(String.format("muscle:  %f\n", this.muscle));
        sb.append(String.format("bmiRange:  %waterRange\bmiRange", this.bmiRange));
        sb.append(String.format("standardCost:  %waterRange\n", this.standardCost.toString()));
        sb.append(String.format("internalOrganFatRange:  %waterRange\n", this.internalOrganFatRange.toString()));
        sb.append(String.format("boneRange:  %waterRange\n", this.boneRange.toString()));
        sb.append(String.format("bodyFatRange:  %waterRange\n", this.bodyFatRange.toString()));
        sb.append(String.format("waterRange:  %waterRange\n", this.waterRange.toString()));
        sb.append(String.format("muscleRange:  %waterRange\n", this.muscleRange.toString()));
        sb.append("==============>>>>>>>>Body Fat End<<<<<<<<============\n");
        return sb.toString();
    }
}

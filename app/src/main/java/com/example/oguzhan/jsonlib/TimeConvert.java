package com.incirrd.bildiricin.jsonlib;

public class TimeConvert {

	public String ConvertMouth(String ay)
	{
		
		if(ay.equals("01"))
			ay="Ocak";

		else if(ay.equals("02"))
			ay="Şubat";

		else if(ay.equals("03"))
			ay="Mart";

		else if(ay.equals("04"))
			ay="Nisan";

		else if(ay.equals("05"))
			ay="Mayıs";

		else if(ay.equals("06"))
			ay="Haziran";

		else if(ay.equals("07"))
			ay="Temmuz";

		else if(ay.equals("08"))
			ay="Ağustos";

		else if(ay.equals("09"))
			ay="Eylül";

		else if(ay.equals("10"))
			ay="Ekim";

		else if(ay.equals("11"))
			ay="Kasım";

		else if(ay.equals("12"))
			ay="Aralık";
		else 
			ay="'-'";
		return ay;
		
		
	}
	
}

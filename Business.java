package hw6;

public class Business {
  String businessID;
  String businessName;
  String businessAddress;
  String reviews;
  int reviewCharCount;
  
  public Business(String a, String b, String c, String d, int e) {
    businessID = a;
    businessName = b;
    businessAddress = c;
    reviews = d;
    reviewCharCount = e;
  }
  public String toString() {
    return "-------------------------------------------------------------------------------\n"
          + "Business ID: " + businessID + "\n"
          + "Business Name: " + businessName + "\n"
          + "Business Address: " + businessAddress + "\n"
          //+ "Reviews: " + reviews + "\n"
          + "Character Count: " + reviewCharCount;
  }

  public String getID() {return businessID;}
  public String getName() {return businessName;}
  public String getAddress() {return businessAddress;}
  public String getReview() {return reviews;}
  public int getChar() {return reviewCharCount;}

}
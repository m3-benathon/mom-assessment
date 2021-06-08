# mom-assessment

Requirements
1. maven installed
2. JAVA 1.8 installed

Database used: MS SQL

Port used: 8090 (you can change the port number by updating the value of "server.port" in application.properties which resides in "src/main/resources".)

Before starting the program, run assessment.sql in scripts folder. Update "spring.datasource.username" and "spring.datasource.password" in application.properties if necessary.

To start the program, open command line, navigate to the folder where the project is downloaded, enter "mvn spring-boot:run".

You can test the APIs through http://localhost:8090/swagger-ui.html (change the port number if you have changed it previously). You can also use other software to test the APIs, e.g. Postman. Make sure the "Content-type" is "application/json" for POST requests.

For creating new household, use the content in create-household-json.txt which resides in json folder. You can change the "housingType" value to any of the following:
- HDB
- Landed
- Condominium

For creating new family member, use the content in create-family-member-without-spouse-json.txt or create-family-member-with-spouse-json.txt which resides in json folder.
You will need the household id from household creation to create family members.
Rules/options/assumptions for all the variables:
1. name
   - any name that the length is not empty and not more than 255 characters
2. gender
   - Male
   - Female
3. maritalStatus
   - Single
   - Married
   - Divorced
   - Windowed
4. occupationType
   - Unemployed
   - Student
   - Employed
5. annualIncome
   - any amount. put 0 for Student and Unemployed
6. dateOfBirth
   - format: yyyy-MM-dd
7. spouse
   - can only be added after the other family member (spouse) is created
   - will auto update the other family member (spouse)
   - spouse must be in the same household
   - require the id of the other family member (spouse)

Grant Schemes APIs:
1. Student Encouragement Bonus
   - available search parameters:
     1. housingTypes - available options: HDB, Landed, Condominium
     2. householdSizeMin - default set as 1
     3. householdSizeMax
     4. totalAnnualIncomeMin
   - default will get all households with at least 1 family member that is under 16 years old and total annual income is less than $150,000
2. Family Togetherness Scheme
   - available search parameters:
     1. housingTypes - available options: HDB, Landed, Condominium
     2. householdSizeMin - default set as 3
     3. householdSizeMax
     4. totalAnnualIncomeMin
     5. totalAnnualIncomeMax
   - default will get all households with at least 1 family member that is under 18 years old, at least 1 family member has spouse of opposite gender and household size of at least 3
3. HDB Elder Bonus
   - available search parameters:
     1. householdSizeMin - default set as 1
     2. householdSizeMax
     3. totalAnnualIncomeMin
     4. totalAnnualIncomeMax
   - default will get all households with housing type is HDB and at least 1 family member is above 50 years old
4. Baby Sunshine Grant
   - available search parameters:
     1. housingTypes - available options: HDB, Landed, Condominium
     2. householdSizeMin - default set as 2
     3. householdSizeMax
     4. totalAnnualIncomeMin
     5. totalAnnualIncomeMax
   - default will get all households with at least one family member that is under 5 years old and household size of at least 2
5. YOLO GST Grant
   - available search parameters:
     1. householdSizeMin - default set as 1
     2. householdSizeMax
     3. totalAnnualIncomeMin
   - default will get all households with total annual income is less than $100,000

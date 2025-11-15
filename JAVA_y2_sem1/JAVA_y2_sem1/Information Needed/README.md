JAVA testing

1.0 COURSEWORK TITLE

AUTOMATED PURCHASE ORDER MANAGEMENT SYSTEM (OWSB)

2.0 THE COURSEWORK OVERVIEW

Omega Wholesale Sdn Bhd (OWSB) is a rapidly growing wholesaler based in Kuala Lumpur, specializing in distributing groceries, fresh produce, and other essential goods to retailers across Malaysia. As business operations expand, OWSB recognizes the need to automate its Purchase Order Management (OWSB) process to enhance efficiency and accuracy.

The procurement process begins with a Purchase Requisition (PR) raised by Sales Managers (SM), who determine the required stock based on sales data. A PR includes essential details such as item codes, quantities, and required delivery dates. Once a PR is submitted, the Purchase Manager (PM) reviews it and generates a Purchase Order (PO)—an official document authorizing procurement from registered suppliers.

To streamline this workflow, a Java-based application must be developed using Object-Oriented Programming (OOP) principles. The system should offer the following functionalities:

· User Authentication & Role-Based Access

· User Registration

· Item Management (Entry, Update, and Deletion)

· Supplier Management (Entry, Update, and Deletion)

· Daily Sales Data Entry

· Purchase Requisition (PR) Creation

· Viewing of PRs

· Purchase Order (PO) Generation

· List & Status of Purchase Orders

In addition a supporting document is needed to reflect the design of the implementation codes and the implementation details that utilises the Object-oriented programming concepts.

3.0 OBJECTIVES OF THIS COURSEWORK

Develop the practical ability to describe, justify, and implement an Object-oriented system.

4.0 TYPE

Group Assignment

5.0 COURSEWORK DESCRIPTION

The developed system should be menu driven with options for the functionalities listed in section 2.0. Menu should be repeated until the user opts to exit the system. You as an Object-oriented programming student need to identify the relationship among the entities and develop the necessary methods needed to fulfil the requirements of the expected systems.

Login access:

You program should have five types of access rights such as Sales Manager, Purchase Manager and Administrator.

1. Sales Manager (junheng) should have access to the following functionalities:

· Item Entry (Add/Save/Delete/Edit)

· Supplier Entry (Add/Save/Delete/Edit)

· Daily Item-wise Sales Entry (Add/Save/Delete/Edit)

· Create a Purchase Requisition (Add/Save/Delete/Edit)

· Display Requisition (View)

· List of Purchaser Orders(View)

2. Purchase Manager (Eden) (PM) and should be allowed to view and access the following functionalities only:

· List of Items (View)

· List of Suppliers (View)

· Display Requisition (View)

· Generate Purchase Order (Add/Save/Delete/Edit)

· List of Purchaser Orders (View)

3. Administrator (Benjamin) should have the rights to access and update all the application functionalities and data. They are the authorized personnel to create the above mentioned three types of users involved in the OWSB system.

4. Inventory Manager(Boon) (IM)

Role: Manages stock, updates inventory, and ensures the system reflects real-time stock levels.

· Functionalities:

o View list of items.

o Update stock based on received items from approved Purchase Orders (POs).

o Manage stock levels and track low-stock alerts.

o Generate stock reports.

o View purchase orders to verify received items.

5. Finance Manager (LeeJuin) (FM)

Role: Handles financial approvals, verifies inventory updates, and processes supplier payments.

· Functionalities:

o Approve purchase orders.

o Verify inventory updates from the Inventory Manager before processing payments.

o Process payments to suppliers.

o Generate financial reports.

o View purchase requisitions and purchase orders.

Some descriptions provided below for better understanding of the process involved in the system.

User Registration:

Your program should allow the Administrators to create and register the details of users involved in the system. Validations are necessary and the system should maintain the uniqueness by having individual identification number for every user. Users should be of type SM or PM or Admin. User details need to be captured in a text file.

Item Entry:

Your program should allow the authorized users to enter the item details like item code, item name and the supplier id who supplies the item to OWSB. Identify the various attributes needed for describing the item entity. No duplication of items allowed. Added items should be saved in a text file. All the items should be supplied by the registered suppliers of your OWSB system.

Supplier Entry:

Your program should allow the authorized users to enter the supplier details like supplier code, supplier name and the item id supplied by the supplier. Identify the various attributes needed for describing the supplier entity. No duplication of supplier allowed. Added suppliers should be saved in a text file.

Inventory Manager:

Your program should allow Inventory Managers to manage stock levels when items are received from approved Purchase Orders (POs), the system should allow the Inventory Manager to update the stock accordingly The Inventory Manager should also be able to generate stock reports.

Finance Manager:

Finance Manager is responsible for approving the PO raised by PM. Not all POs need to be approved. But once the PO is raised by PM, the newly raised PO should be available in the PO list for the FM to be approved. This FM should have the right to modify the quantity and choose from the list of suppliers supplying the same item in the PO.

Daily Item-wise Sales Entry

This option allows the users with access to enter the total sales of every item daily. This sale need be updated to the stock of the item accordingly.

Create a PR:

Your program should allow the users with access to place a purchase requisition (PR). PR should be generated for the items to be purchased to the OWSB from the respective suppliers. PR typically contains the item code, quantity and the date by when it is required. It also should fetch the respective supplier code of the item. A unique identifier is required to track the PR and also the SM, who raised the PR need to be captured and saved in text file.

Display a PR:

Your program should allow the authorized users to view all the PRs raised by all sales managers.

Generate PO

Purchase Orders (PO) need to be generated only by Purchase Managers (PM). They are the only authorized users who can raise the PO. POs are basically approved Purchase

Requisition raised by the Sales Managers (SM). A unique identifier is required to track the PO and also the PM, who raised the PO need to be captured and saved in text files

List of Purchase Orders:

Your program should allow the users with access to view the report of purchase orders raised by the Purchase Mangers (PM).

6.0 GENERAL REQUIREMENTS

The program submitted should compile and be executed without errors

Validation should be done for each entry from the users in order to avoid logical errors. The implementation code must highlight the use of Object-oriented programming concepts as required by the solution.

7.0 DELIVERABLES:

§ Documents delivered softcopy form.

§ GUI design is an requirement

§ Submission deadline:

7.1 DOCUMENTS: COURSEWORK REPORT

ü As part of the assessment, you must submit the project report in softcopy form, which should have the following format:

A) Cover Page:

All reports must be prepared with a front cover. A protective transparent plastic sheet can be placed in front of the report to protect the front cover. The front cover should be presented with the following details:

Ä Module

Ä Coursework Title

Ä Intake

Ä Student name and id

Ä Date Assigned (the date the report was handed out).

Ä Date Completed (the date the report is due to be handed in).

B) Contents:

Ä Description and justification of Object-oriented programming concepts incorporated into the solution

Ä Design solution

Ä Screenshots of output of the program with appropriate explanations

C) Limitation and Conclusion

D) References

Ä

gg
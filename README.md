My Expenses is a simple Android application designed for tracking cash expenses. The application allows users to record their expenses quickly and keep an overview of their spending directly on the mobile device. The project was created as a graduation project and focuses on simplicity, clarity, and reliability.

The main goal of this project was to develop an easy-to-use Android application that enables users to record cash expenses, store data locally on the device, and access the stored information even after restarting the application. The application works without an internet connection, which makes it suitable for everyday use.

The application was developed using Android Studio as the development environment and the Kotlin programming language for implementing the application logic. The user interface was created using XML layouts, which allowed a clear and structured design of the main screen. For data storage, the application uses SharedPreferences, a simple local storage mechanism provided by the Android operating system.

SharedPreferences was chosen because the application works with a small amount of data and does not require complex database operations. All expense records are stored locally in the device’s internal storage and are automatically loaded when the application is launched. This approach ensures fast data access and reliable operation during regular use.

The application provides basic functionality necessary for expense tracking, including adding new expenses with a name, amount, and date, displaying a list of saved expenses, and removing selected records from the list. The date of each expense is generated automatically to simplify data entry for the user.

Although the current version of the application uses a simple storage solution, it could be extended in the future by implementing a database solution such as SQLite, which would allow more advanced features like categorization of expenses or statistical analysis.              

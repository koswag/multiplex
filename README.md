## Ticket booking app

The goal is to build a seat reservation system for a multiplex.

### Business scenario (use case)
1. The user selects the day and the time when he/she would like to see the movie.
2. The system lists movies available in the given time interval - title and screening
   times.
3. The user chooses a particular screening.
4. The system gives information regarding screening room and available seats. 
5. The user chooses seats, and gives the name of the person doing the reservation (name and surname).
6. The system gives back the total amount to pay and reservation expiration time.
   
### Assumptions
1. The system covers a single cinema with multiple rooms (multiplex). 
2. Seats can be booked at latest 15 minutes before the screening begins. 
3. Screenings given in point of the scenario should be sorted by title and screening time.
4. There are three ticket types: adult (25 PLN), student (18 PLN), child (12.50 PLN).

### Business requirements
1. The data in the system should be valid, in particular:
   1. name and surname should each be at least three characters long, starting
      with a capital letter. 
   2. The surname could consist of two parts separated with a
      single dash, in this case the second part should also start with a capital letter. 
   3. Reservation applies to at least one seat.
2. There cannot be a single place left over in a row between two already reserved
   places.
3. The system should properly handle Polish characters.

### Technical requirements
1. Application must be written in JVM language (Java, Scala, Kotlin etc.)
2. Operations must be exposed as REST services
3. No need to stick to any particular database - relational, NoSQL or in-memory
   database is fine
4. No need to build frontend

### Demo
1. Include shell script that will build and run your app.
2. The system should be automatically initialized with test data (at least three screening
   rooms, three movies and two screenings per room).
3. Include shell script that would run whole use case calling respective endpoints (using
   e.g. curl), we want to see requests and responses in action.
   
### Before submitting...
1. Make sure your solution contains a README file, which explains how to build and
   run your project and demo.
2. If there are some additional assumptions you’ve made, put them in README as well.
3. Prepare a single pull request containing whole source code (so that we can easily do
   a code review for you).

## Additional tasks (choose one)

### Reservation confirmation

#### Extension to the main scenario
1. In the last step, in addition to the total amount and reservation expiration time, a
   confirmation link (let’s suppose this link would be sent by email in typical system)
   should also be given
2. User accesses the link to confirm reservation

#### Additional requirements
1. If the user does not confirm the reservation in 15 minutes (but not later than 15
   minutes before the screening), the system should cancel the reservation. 
2. Reservation cancellation should happen in two cases:
    1. 15 minutes after the reservation is made,
    2. 15 minutes before the screening.

### Seat recommendation

#### Extension to the main scenario
1. When choosing seats in point 3. of the main scenario, the user also gives the number
   of seats he/she would like to book.
2. The system gives auditorium schema with recommended seats.

#### Additional requirements
1. The system recommends mostly the places in a single row, next to each other 
2. The further from the screen and the closer to the center, the better the place is. 
3. Use the Manhattan metric to rate each seat, where the distance between two rows is
   twice the distance between two seats in a row. Example (assuming lower rate is better):

   screen

   6 5 4 5 6

   4 3 2 3 4

   2 1 0 1 2
4. Script demonstrating system should visualize auditorium before booking the seats,
   e.g. dot - free seat, x - reserved seat, o - recommended seat
5. You can demonstrate how the recommendation system works in any way, but put the
   description in the README.

### Different ticket prices

#### Additional requirements
1. Ticket price should be 4 PLN higher during the weekend (Friday 14:00 PM til Sunday
   11:00 PM)
2. User can provide voucher code which gives 50% discount

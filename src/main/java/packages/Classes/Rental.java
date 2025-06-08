package packages.Classes;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rental {
    private int id;
    private int userId;
    private int bookId;
    private Book book;
    private LocalDate rentDate;
    private LocalDate returnDate;
}

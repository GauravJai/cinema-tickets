import org.junit.Before;
import org.junit.Test;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TicketServiceImplTest {

    private TicketServiceImpl ticketService;
    private SeatReservationService seatReservationService;
    private TicketPaymentService ticketPaymentService;

    @Before
    public void setup() {
        seatReservationService = mock(SeatReservationService.class);
        ticketPaymentService = mock(TicketPaymentService.class);
        ticketService = new TicketServiceImpl(seatReservationService, ticketPaymentService);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void givenNoAdultAnd1Infant_WhenInvalidTicketRequests_AndPurchaseTickets_ThenShouldThrowInvalidPurchaseException() {
        // Given
        long accountId = 1;
        TicketTypeRequest[] ticketRequests = {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 0),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        };

        // When
        ticketService.purchaseTickets(accountId, ticketRequests);

        // Then
        // Expecting InvalidPurchaseException
    }

    @Test(expected = InvalidPurchaseException.class)
    public void givenNoAdult_WhenPurchaseTickets_ThenShouldThrowInvalidPurchaseException() {
        // Given
        long accountId = 1;
        TicketTypeRequest[] ticketRequests = {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 0),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        };

        // When
        ticketService.purchaseTickets(accountId, ticketRequests);

        // Then
        // Expecting InvalidPurchaseException
    }

    @Test(expected = InvalidPurchaseException.class)
    public void given21NoOfTickets_WhenPurchaseTickets_ThenShouldThrowInvalidPurchaseException() {
        // Given
        long accountId = 1;
        TicketTypeRequest[] ticketRequests = {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 15),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 5),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1),
        };

        // When
        ticketService.purchaseTickets(accountId, ticketRequests);

        // Then
        // Expecting InvalidPurchaseException
    }

    @Test
    public void givenValidTicketRequests_AndPurchaseTickets_ThenShouldMakePaymentAndReserveSeats() {
        // Given
        long accountId = 1;
        int expectedAmountToPay = 50; // 2*20 + 1*10 = 50
        int expectedSeatToAllocate = 3; // No seat for Infant - 2 + 1 = 3
        TicketTypeRequest[] ticketRequests = {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        };

        // When
        ticketService.purchaseTickets(accountId, ticketRequests);

        // Then
        verify(ticketPaymentService).makePayment(accountId, expectedAmountToPay);
        verify(seatReservationService).reserveSeat(accountId, expectedSeatToAllocate);
    }
}
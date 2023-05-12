package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     */

    private static final int INFANT_TICKET_PRICE = 0;
    private static final int CHILD_TICKET_PRICE = 10;
    private static final int ADULT_TICKET_PRICE = 20;
    private static final int MAX_TICKETS = 20;

    private final SeatReservationService seatReservationService;
    private final TicketPaymentService ticketPaymentService;

    public TicketServiceImpl(SeatReservationService seatReservationService, TicketPaymentService ticketPaymentService) {
        this.seatReservationService = seatReservationService;
        this.ticketPaymentService = ticketPaymentService;
    }

    /***
     * validates
     *  1. max no of ticket <= 20
     *  2. at least one adult is accompanied
     * @param ticketRequests {@link TicketTypeRequest} is request object for ticket booking
     */
    private void validateTicketRequests(TicketTypeRequest... ticketRequests) {
        int totalTickets = 0;
        int totalAdultTickets = 0;
        for (TicketTypeRequest ticketRequest : ticketRequests) {
            totalTickets += ticketRequest.getNoOfTickets();
            if (ticketRequest.getTicketType() == TicketTypeRequest.Type.ADULT) {
                totalAdultTickets += ticketRequest.getNoOfTickets();
            }
        }
        if (totalTickets > MAX_TICKETS) {
            throw new InvalidPurchaseException("Exceeded maximum number of tickets per purchase.");
        }
        if (totalAdultTickets == 0 && totalTickets > 0) {
            throw new InvalidPurchaseException("At least one adult ticket is required for the purchase.");
        }
    }

    /***
     * calculate total ticket price for a booking request
     * @param ticketRequests {@link TicketTypeRequest} is request object for ticket booking
     * @return totalPrice
     */
    private int calculateTotalPrice(TicketTypeRequest... ticketRequests) {
        int totalPrice = 0;

        for (TicketTypeRequest ticketRequest : ticketRequests) {
            int ticketPrice = getTicketPrice(ticketRequest.getTicketType());
            totalPrice += ticketPrice * ticketRequest.getNoOfTickets();
        }

        return totalPrice;
    }

    /***
     * maps ticket type to ticket price
     * @param ticketType {@link TicketTypeRequest.Type} an {@link Enum} of allowed types of tickets
     * @return ticketPrice
     */
    private int getTicketPrice(TicketTypeRequest.Type ticketType) {
        switch (ticketType) {
            case INFANT:
                return INFANT_TICKET_PRICE;
            case CHILD:
                return CHILD_TICKET_PRICE;
            case ADULT:
                return ADULT_TICKET_PRICE;
            default:
                throw new InvalidPurchaseException("Invalid ticket type.");
        }
    }

    /***
     * calculate ticket count for a given type
     * @param ticketRequests an array of {@link TicketTypeRequest}, which is request object for ticket booking
     * @param ticketType {@link TicketTypeRequest.Type} an {@link Enum} of allowed types of tickets
     * @return totalCount
     */
    private int countTotalTickets(TicketTypeRequest[] ticketRequests, TicketTypeRequest.Type ticketType) {
        int totalCount = 0;
        for (TicketTypeRequest ticketRequest : ticketRequests) {
            if (ticketRequest.getTicketType() == ticketType) {
                totalCount += ticketRequest.getNoOfTickets();
            }
        }
        return totalCount;
    }

    /***
     * validates request and calls third party apis to makes payment and reserve seats
     * @param accountId account id of ticket booking account
     * @param ticketTypeRequests an array of {@link TicketTypeRequest}
     * @throws InvalidPurchaseException when ticket booking request is invalid
     */
    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests)
            throws InvalidPurchaseException {
        validateTicketRequests(ticketTypeRequests);

        int amount = calculateTotalPrice(ticketTypeRequests);
        int totalAdultTickets = countTotalTickets(ticketTypeRequests, TicketTypeRequest.Type.ADULT);
        int totalChildTickets = countTotalTickets(ticketTypeRequests, TicketTypeRequest.Type.CHILD);

        ticketPaymentService.makePayment(accountId, amount);
        seatReservationService.reserveSeat(accountId, totalAdultTickets + totalChildTickets);
    }

}



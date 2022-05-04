package basketball.server;

import basketball.model.User;
import basketball.model.Match;
import basketball.model.Ticket;
import basketball.network.protobuffprotocol.BasketballProtobufs.MatchDto;
import basketball.network.protobuffprotocol.BasketballProtobufs.UserDto;
import basketball.network.protobuffprotocol.BasketballProtobufs.TicketDto;

public class DtoUtils {
    public static UserDto fromUser(User user) {
        return UserDto.newBuilder()
                .setId(user.getId())
                .setUsername(user.getUsername())
                .setPassword(user.getPassword())
                .build();
    }

    public static MatchDto fromMatch(Match match) {
        return MatchDto.newBuilder()
                .setId(match.getId())
                .setName(match.getName())
                .setTicketPrice(match.getTicketPrice())
                .setNoAvailableSeats(match.getNoAvailableSeats())
                .build();
    }

    public static Ticket fromTicketDto(TicketDto ticketDto) {
        return new Ticket(ticketDto.getId(), ticketDto.getClientName(), ticketDto.getNoSeats(), ticketDto.getMatchId());
    }
}

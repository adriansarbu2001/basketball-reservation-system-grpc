using BasketballModel;
using BasketballNetworking;

namespace BasketballGrpcClient
{
    public class DtoUtils
    {
        public static UserDto fromUser(Account user)
        {
            return new UserDto {Id = user.AccountId, Password = user.Password, Username = user.Username};
        }

        public static Account fromUserDto(UserDto userDto)
        {
            return new Account {AccountId = userDto.Id, Password = userDto.Password, Username = userDto.Username};
        }

        public static MatchDto fromMatch(Match match)
        {
            return new MatchDto
            {
                Id = match.MatchId, Name = match.Name, TicketPrice = match.TicketPrice,
                NoAvailableSeats = match.NoAvailableSeats
            };
        }

        public static Match fromMatchDto(MatchDto matchDto)
        {
            return new Match()
            {
                MatchId = matchDto.Id, Name = matchDto.Name, TicketPrice = matchDto.TicketPrice,
                NoAvailableSeats = matchDto.NoAvailableSeats
            };
        }

        public static Ticket fromTicketDto(TicketDto ticketDto)
        {
            return new Ticket
            {
                TicketId = ticketDto.Id, ClientName = ticketDto.ClientName, NoSeats = ticketDto.NoSeats,
                MatchId = ticketDto.MatchId
            };
        }
    }
}

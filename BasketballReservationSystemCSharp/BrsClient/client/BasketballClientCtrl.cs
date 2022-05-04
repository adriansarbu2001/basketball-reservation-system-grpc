using System;
using System.Collections;
using System.Collections.Generic;
using System.Data.SqlClient;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using BasketballModel;
using BasketballNetworking;
using Grpc.Core;

namespace BasketballGrpcClient
{
    public class ControllerException : Exception
    {
        public ControllerException() : base()
        {
        }

        public ControllerException(String msg) : base(msg)
        {
        }

        public ControllerException(String msg, Exception ex) : base(msg, ex)
        {
        }
    }

    public class BasketballClientCtrl
    {
        public event EventHandler<BasketballUserEventArgs> updateEvent;
        private readonly BasketballService.BasketballServiceClient client;
        private Account currentUser;
        private AsyncDuplexStreamingCall<BasketballRequest, BasketballResponse> stream;

        public BasketballClientCtrl(BasketballService.BasketballServiceClient client)
        {
            this.client = client;
            currentUser = null;
        }

        public void login(String username, String password)
        {
            UserDto userDto = new UserDto() {Username = username, Password = password};
            
            var loginResponse = client.login(new BasketballRequest(){Type = BasketballRequest.Types.Type.Login, UserDto = userDto});

            if (loginResponse.Type == BasketballResponse.Types.Type.Error)
            {
                throw new ControllerException(loginResponse.Error);
            }

            if (loginResponse.Type == BasketballResponse.Types.Type.Ok)
            {
                currentUser = DtoUtils.fromUserDto(loginResponse.UserDto);
                Console.WriteLine("Ctrl Login succeeded ...");
                Console.WriteLine("Current user {0}", currentUser);

                this.stream = client.subscribe();
                var subscribeResponse = Task.Run(async () =>
                {
                    while (await stream.ResponseStream.MoveNext(CancellationToken.None))
                    {
                        ticketSold();
                    }
                });
            }
        }

        public async void logout()
        {
            Console.WriteLine("Ctrl logout");
            client.logout(new BasketballRequest(){Type = BasketballRequest.Types.Type.Logout, UserDto = DtoUtils.fromUser(currentUser)});
            currentUser = null;
            await this.stream.RequestStream.CompleteAsync();
            stream.Dispose();
        }

        public IEnumerable<Match> findAll()
        {
            var response = client.findAll(new BasketballRequest(){Type = BasketballRequest.Types.Type.FindAllMatch});
            List<Match> result = new List<Match>();

            if (response.Type == BasketballResponse.Types.Type.Error)
            {
                throw new ControllerException(response.Error);
            }

            if (response.Type == BasketballResponse.Types.Type.Ok)
            {
                foreach (MatchDto matchDto in response.MatchDtos.ToList())
                {
                    result.Add(DtoUtils.fromMatchDto(matchDto));
                }
            }

            return result;
        }

        public async Task saveTicket(string client_name, string no_seats, string match_id)
        {
            TicketDto ticketDto = new TicketDto() {ClientName = client_name, NoSeats = Int32.Parse(no_seats), MatchId = Int32.Parse(match_id)};
            
            var response = client.saveTicket(new BasketballRequest(){Type = BasketballRequest.Types.Type.SaveTicket, TicketDto = ticketDto});
            
            if (response.Type == BasketballResponse.Types.Type.Error)
            {
                throw new ControllerException(response.Error);
            }

            if (response.Type == BasketballResponse.Types.Type.Ok)
            {
                Console.WriteLine("Notifying all...");
                await this.stream.RequestStream.WriteAsync(new BasketballRequest());
            }
        }

        public IEnumerable<Match> availableMatchesDescending()
        {
            var response = client.availableMatchesDescending(new BasketballRequest(){Type = BasketballRequest.Types.Type.AvailableMatchesDescending});
            List<Match> result = new List<Match>();

            if (response.Type == BasketballResponse.Types.Type.Error)
            {
                throw new ControllerException(response.Error);
            }

            if (response.Type == BasketballResponse.Types.Type.Ok)
            {
                foreach (MatchDto matchDto in response.MatchDtos.ToList())
                {
                    result.Add(DtoUtils.fromMatchDto(matchDto));
                }
            }

            return result;
        }

        public void ticketSold()
        {
            Console.WriteLine("Ticket sold");
            BasketballUserEventArgs userEventArgs = new BasketballUserEventArgs(BasketballUserEvent.TicketSold, null);
            OnUserEvent(userEventArgs);
        }

        protected virtual void OnUserEvent(BasketballUserEventArgs e)
        {
            if (updateEvent == null) return;
            updateEvent(this, e);
            Console.WriteLine("Update Event called");
        }
    }
}

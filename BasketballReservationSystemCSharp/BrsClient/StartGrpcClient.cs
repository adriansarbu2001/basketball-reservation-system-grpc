using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Windows.Forms;
using BasketballNetworking;
using Grpc.Core;

namespace BasketballGrpcClient
{
    static class StartGrpcClient
    {
        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        [STAThread]
        static void Main()
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            
            AppContext.SetSwitch("System.Net.Http.SocketsHttpHandler.Http2UnencryptedSupport", true);
            Channel channel = new Channel("127.0.0.1:55556", ChannelCredentials.Insecure);
            var client = new BasketballService.BasketballServiceClient(channel);
            BasketballClientCtrl ctrl = new BasketballClientCtrl(client);

            Application.Run(new LoginForm(ctrl));
        }
    }
}

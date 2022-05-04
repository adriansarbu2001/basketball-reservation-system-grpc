using System;
using System.Windows.Forms;

namespace BasketballGrpcClient
{
    public partial class LoginForm : Form
    {
        private BasketballClientCtrl ctrl;
        public LoginForm(BasketballClientCtrl ctrl)
        {
            InitializeComponent();
            this.ctrl = ctrl;
            textBox1.Text = "seller1";
            textBox2.Text = "seller1";
        }

        private void LoginForm_Load(object sender, EventArgs e)
        {
            
        }
        
        private void buttonLogIn_Click(object sender, EventArgs e)
        {
            try
            {
                ctrl.login(textBox1.Text, textBox2.Text);
                
                SellerForm sellerForm = new SellerForm(ctrl);
                textBox1.Clear();
                textBox2.Clear();
                this.Hide();
                sellerForm.ShowDialog();
                this.Show();
            }
            catch (ControllerException ex)
            {
                MessageBox.Show(ex.Message);
            }
        }
    }
}

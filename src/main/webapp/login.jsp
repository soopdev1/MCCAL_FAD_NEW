<%@page import="it.refill.engine.Action"%>
<!DOCTYPE html>
<html lang="en">

    <head>

        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
        <meta name="description" content="">
        <meta name="author" content="">
        <title>Login</title>
        <link href="vendor/fontawesome-free/css/all.min.css" rel="stylesheet" type="text/css">
        <link href="css/googlefontcss.css" rel="stylesheet">
        <link href="css/sb-admin-2.min.css" rel="stylesheet">
        <link rel="shortcut icon" href="favblu.png" />    
    </head>

    <body class="bg-gradient-primary">

        <div class="container">

            <!-- Outer Row -->
            <div class="row justify-content-center">

                <div class="col-xl-10 col-lg-12 col-md-9">

                    <div class="card o-hidden border-0 shadow-lg my-5">
                        <div class="card-body p-0">
                            <!-- Nested Row within Card Body -->
                            <div class="row">
                                <div class="col-lg-6 d-none d-lg-block bg-login-image"></div>
                                <div class="col-lg-6">
                                    <div class="p-5">
                                        <div class="text-center">
                                            <h1 class="h4 text-gray-900 mb-4">Portale FAD</h1>
                                        </div>
                                        <%if (Action.test) {%>
                                        
                                        <form class="login100-form validate-form" method="post" action="Login?type=login_conference" name="CO">
                                            
                                            <!--
                                            <input type="hidden" id="id" name="id" value="15">
                                            <input type="hidden" id="user" name="user" value="raffaele.cosco@faultless.it">
                                            <input type="hidden" id="password" name="password" value="QDrsNKVz">
                                            <input type="hidden" id="view" name="view" value="1">
                                            -->
                                            
                                            <button class="btn btn-primary btn-user btn-block">
                                                Login
                                            </button>
                                            
                                        </form>
                                        <%}%>   
                                        <hr>
                                        <hr>
                                        <hr>
                                        <hr>
                                        <div class="px-3 py-5 bg-gradient-primary text-white">Per effettuare l'accesso e' necessario farlo tramite apposito portale o tramite email ricevuta.</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

            </div>

        </div>

    </div>

    <!-- Bootstrap core JavaScript-->
    <script src="vendor/jquery/jquery.min.js"></script>
    <script src="vendor/bootstrap/js/bootstrap.bundle.min.js"></script>

    <!-- Core plugin JavaScript-->
    <script src="vendor/jquery-easing/jquery.easing.min.js"></script>

    <!-- Custom scripts for all pages-->
    <script src="js/sb-admin-2.min.js"></script>

</body>

</html>

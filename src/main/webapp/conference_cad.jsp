<%@page import="java.util.List"%>
<%@page import="it.refill.engine.GenericUser"%>
<%@page import="it.refill.engine.Action"%>
<!DOCTYPE html>
<html lang="en">

    <head>

        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
        <meta name="description" content="">
        <meta name="author" content="">

        <title>Stanza</title>

        <!-- Custom fonts for this template-->
        <link href="vendor/fontawesome-free/css/all.min.css" rel="stylesheet" type="text/css">
        <link href="css/googlefontcss.css" rel="stylesheet">
        <!-- Custom styles for this template-->
        <link href="css/sb-admin-2.min.css" rel="stylesheet">
        <script src="js/jquery-3.4.1.js"></script>
        <script src="js/external_api.js"></script>
        <script src="js/lib-jitsi-meet.min.js"></script>

    </head>

    <%
        String us_role = Action.getSessionValue(session, "us_role");
        String us_nome = Action.getSessionValue(session, "us_nome");
        String us_cognome = Action.getSessionValue(session, "us_cognome");
        String us_stanza = Action.getSessionValue(session, "us_stanza");
        if (us_stanza.equals("")) {
            us_stanza = "TESTROOM";
        }
    %>


    <script>

        function login() {
            log_ajax('L1', '<%=us_stanza%>', '<%=us_role%>' + ';' + '<%=us_nome%>');
            document.getElementById('startbutton').click();
        }

        function start(roomname) {
            var name = "<%=us_nome%>" + " " + "<%=us_cognome%>";
            var domain = "<%=Action.getDomainFAD()%>";
            $('#content-jitsi').html("");
            var but1 = ['microphone', 'camera', 'fullscreen', 'hangup', 'chat', 'desktop'];
            var options = {
                roomName: roomname,
                noSSL: false,
                enableWelcomePage: false,
                parentNode: document.getElementById('content-jitsi'),
                configOverwrite: {
                    enableWelcomePage: false
                },
                interfaceConfigOverwrite: {
                    TOOLBAR_BUTTONS: but1
                }
            };
            var api = new JitsiMeetExternalAPI(domain, options);
            api.executeCommand('displayName', name);

        }

        function log_ajax(type, room, action) {
            $.ajax({
                url: "Print",
                type: "POST",
                data: {"type": type, "room": room, "action": action},
                success: function (result) {
                }
            }
            );
        }

        function logout() {
            $.ajax({
                type: "POST",
                url: "Login?type=logout"
            });
            window.location.href = "login.jsp";
        }

        function loadingpage() {
            document.getElementById('startbutton').click();
            document.getElementById('sidebarToggle').click();
        }

    </script>
    <style type="text/css">
        #content-jitsi {
            bottom: 0;
            right: 0;
            width: 100%;
            height: 100vh;
            overflow: hidden;
        }
    </style>
    <body id="page-top" onload="return loadingpage();" >

        <!-- Page Wrapper -->
        <div id="wrapper">

            <!-- Sidebar -->
            <ul class="navbar-nav bg-gradient-primary sidebar sidebar-dark accordion" id="accordionSidebar">

                <!-- Sidebar - Brand -->
                <a class="sidebar-brand d-flex align-items-center justify-content-center" href="conference.jsp" onclick="return false;">
                    <div class="sidebar-brand-icon rotate-n-15">
                        <i class="fas fa-laugh-wink"></i>
                    </div>
                    <div class="sidebar-brand-text mx-3">Conference <sup>2.0</sup></div>
                </a>

                <!-- Divider -->
                <hr class="sidebar-divider my-0">

                <!-- Nav Item - Dashboard -->
                <li class="nav-item active" >
                    <a class="nav-link" href="conference.jsp"  onclick="return false;">
                        <i class="fas fa-fw fa-tachometer-alt"></i>
                        <span>Principal Room</span></a>
                </li>

                <!-- Divider -->
                <hr class="sidebar-divider">

                <li class="nav-item">
                    <a class="nav-link" href="#"  onclick="return logout();">
                        <i class="fas fa-sign-out-alt fa-sm text-white-50"></i> LOGOUT</a>
                </li>

                <!-- Sidebar Toggler (Sidebar) -->
                <div style="display: none;">
                    <button id="sidebarToggle"></button>
                </div>

            </ul>
            <!-- End of Sidebar -->

            <!-- Content Wrapper -->
            <div id="content-wrapper" class="d-flex flex-column">
                <div id="content">
                    <nav class="navbar navbar-expand navbar-light bg-white topbar mb-4 static-top shadow">
                        <button id="sidebarToggleTop" class="btn btn-link d-md-none rounded-circle mr-3">
                            <i class="fa fa-bars"></i>
                        </button>
                        <div class="d-none d-sm-inline-block form-inline mr-auto ml-md-6 my-4 my-md-0 mw-100 navbar-search">
                            <b>STANZA:</b> <%=us_stanza%>
                        </div>
                        <div class="topbar-divider d-none d-sm-block"></div>
                        <div class="d-none d-sm-inline-block form-inline mr-auto ml-md-6 my-4 my-md-0 mw-100 navbar-search">   
                            <b><%=us_role%>:</b> <%=us_nome%> <%=us_cognome%>
                        </div>
                    </nav>
                    <div class="container-fluid">
                        <!-- Page Heading -->
                        <div class="row">
                            <!-- Area Chart -->
                            <div class="col-xl-12 col-lg-12">
                                <!-- Card Header - Dropdown -->
                                <div id="content-jitsi">
                                    <a class="btn btn-primary" type="button" id="startbutton" href="#intro"  onclick="return start('<%=us_stanza%>');" >
                                        <i class="fa fa-video-camera"></i> AVVIA CONFERENCE</a></li> 
                                </div>
                            </div>
                        </div>
                    </div>
                    <!-- /.container-fluid -->
                </div>
                <!-- End of Main Content -->

                <!-- Footer -->
                <footer class="sticky-footer bg-white">
                    <div class="container my-auto">
                        <div class="copyright text-center my-auto">
                            <span>Copyright &copy; 2020</span>
                        </div>
                    </div>
                </footer>
                <!-- End of Footer -->

            </div>
            <!-- End of Content Wrapper -->

        </div>
        <!-- End of Page Wrapper -->

        <!-- Scroll to Top Button-->
        <a class="scroll-to-top rounded" href="#page-top">
            <i class="fas fa-angle-up"></i>
        </a>

        <!-- Logout Modal-->
        <!-- Bootstrap core JavaScript-->
        <script src="vendor/jquery/jquery.min.js"></script>
        <script src="vendor/bootstrap/js/bootstrap.bundle.min.js"></script>

        <!-- Core plugin JavaScript-->
        <script src="vendor/jquery-easing/jquery.easing.min.js"></script>

        <!-- Custom scripts for all pages-->
        <script src="js/sb-admin-2.min.js"></script>

        <link href="js/select2.min.css" rel="stylesheet" />
        <script src="js/select2.min.js"></script>

        <script>
                                $(document).ready(function () {
                                    $('.js-example-basic-single').select2({
                                        placeholder: "...",
                                        theme: 'classic'
                                    });
                                });
        </script>
    </body>

</html>

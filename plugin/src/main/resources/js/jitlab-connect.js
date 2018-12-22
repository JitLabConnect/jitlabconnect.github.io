(function ($) {

    // form the URL
    var tokenSize = 20;
    var restUrl = AJS.contextPath() + "/rest/jitlab-connect/1.0/";
    var servletUrl = window.location.protocol.concat("//")
                                      .concat(window.location.host)
                                      .concat(AJS.contextPath())
                                      .concat("/plugins/servlet/jitlab");

    $(document).ready(function() {
        $("#jitlab-url").text(servletUrl);

        // request the config information from the server
        $.ajax({
            url: restUrl,
            dataType: "json"
        }).done(function(config) { // when the configuration is returned...
            // ...populate the form.
            $("#jitlab-token").val(config.token);
            $("#jitlab-user").val(config.user);
            $("#jitlab-mapping").val(config.mapping);
            $("#jitlab-commit").val(config.commit).change();
            $("#jitlab-merge1").val(config.mergeOpen).change();
            $("#jitlab-merge2").val(config.mergeReopen).change();
            $("#jitlab-merge3").val(config.mergeMerge).change();
            $("#jitlab-merge4").val(config.mergeClose).change();
            $("#jitlab-allissues").attr('checked', config.allIssues == '1');
            $("#jitlab-link-commit").attr('checked', config.linkCommit == '1');
            $("#jitlab-link-merge").attr('checked', config.linkMerge == '1');

            // update validation
            validate()

            // add listeners
            $("#jitlab-generate").click(function(){
                generateToken();
            });

            $("#jitlab-token").on('input', function() {
                validateToken();
            });

            // submit
            AJS.$("#jitlab-admin").submit(function(e) {
                e.preventDefault();
                if (validate()) {
                    updateConfig();
                }
            });
        });
    });

    function validate() {
        return validateToken();
    }

    function validateToken() {
        if ((new RegExp("^[a-zA-Z]{" + tokenSize + "}$")).test($("#jitlab-token").val())) {
            $("#jitlab-token-error").hide();
            return true;
        }

        $("#jitlab-token-error").show();
        return false;
    }

    function generateToken() {
        var result = "";
        var base = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

        for (var i = 0; i < tokenSize; i++) {
          result += base.charAt(Math.floor(Math.random() * base.length));
        }
        $("#jitlab-token").val(result);
        validateToken();
    }

    function updateConfig() {
        var configObj = {
            token: AJS.$("#jitlab-token").val().trim(),
            user: AJS.$("#jitlab-user").val().trim(),
            mapping: AJS.$("#jitlab-mapping").val().trim(),
            commit: AJS.$("#jitlab-commit").val().trim(),
            mergeOpen: AJS.$("#jitlab-merge1").val().trim(),
            mergeReopen: AJS.$("#jitlab-merge2").val().trim(),
            mergeMerge: AJS.$("#jitlab-merge3").val().trim(),
            mergeClose: AJS.$("#jitlab-merge4").val().trim(),
            allIssues: (AJS.$("#jitlab-allissues").is(':checked'))? "1": "0",
            linkCommit: (AJS.$("#jitlab-link-commit").is(':checked'))? "1": "0",
            linkMerge: (AJS.$("#jitlab-link-merge").is(':checked'))? "1": "0"
        }

        AJS.$.ajax({
            url: restUrl,
            type: "PUT",
            contentType: "application/json",
            data: JSON.stringify(configObj),
            processData: false,
            success: function(data) {
                if (data.status == "ok") {
                    AJS.messages.success("#jitlab-messages", {
                       title: data.message
                    });
                } else {
                    AJS.messages.error("#jitlab-messages", {
                       title: data.message
                    });
                }
            },
            error: function (error) {
                AJS.messages.error("#jitlab-messages", {
                   title: 'Something went wrong'
                });
            }
        });
    }
})(AJS.$ || jQuery);


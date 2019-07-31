(function ($) {
    // form the URL
    var tokenSize = 20;
    var restUrl = AJS.contextPath() + "/rest/jitlab-connect/1.0/";
    var servletUrl = window.location.protocol.concat("//")
                                      .concat(window.location.host)
                                      .concat(AJS.contextPath())
                                      .concat("/plugins/servlet/jitlab");
    var config;

    $(document).ready(function() {
        AJS.inlineHelp();
        $("#jitlab-url").text(servletUrl);

        // request the config information from the server
        $.ajax({
            url: restUrl,
            dataType: "json"
        }).done(function(request) { // when the configuration is returned...
            config = request;

            // ...populate base config the form.
            $("#jitlab-token").val(config.token);
            $("#jitlab-user").val(config.user);
            $("#jitlab-mapping").val(config.mapping);
            $("#jitlab-searchbyname").attr('checked', config.searchByName);
            $("#jitlab-allissues").attr('checked', config.allIssues);

            // add listeners
            initListeners();

            // add projects to dropdown
            for (var key in config.projectConfigs) {
                addProjectOption(key, config.projectConfigs[key].title);
            }

            // switch to default config
            $("#jitlab-project").val("").change();

            // update validation
            validate()

            // submit
            $(document).keypress(
              function(event){
                if (event.which == '13') {
                  event.preventDefault();
                }
            });

            AJS.$("#jitlab-admin").submit(function(e) {
                e.preventDefault();
                if (validate()) {
                    updateConfig();
                }
            });
        });
    });

    function initListeners() {
        $('#jitlab-project').on('change', function() {
            switchProject();
        });

        $("#jitlab-token").on('input', function() {
            config.token = $(this).val().trim();
            validateToken();
        });

        $("#jitlab-user").on('input', function() {
            config.user = $(this).val().trim();
        });

        $("#jitlab-mapping").on('input', function() {
            config.mapping = $(this).val().trim();
        });

        $("#jitlab-allIssues").change(function() {
            config.allIssues = $(this).is(':checked');
        });

        $("#jitlab-searchbyname").change(function() {
            config.searchByName = $(this).is(':checked');
        });

        $("#jitlab-generate").click(function(){
            generateToken();
        });

        $("#jitlab-project-add").click(function() {
            // TODO configuration type
            if (newProject($("#jitlab-project-add-text").val(), "1", "")) {
                $("#jitlab-project-add-text").val("");
            }
        });

        /*$("jitlab-project-add-copy").click(function() {
            // TODO configuration type
            if (newProject($("#jitlab-project-add-text").val(), "1", $("#jitlab-project").val())) {
                $("#jitlab-project-add-text").val("");
            }
        });*/

        $("#jitlab-project-delete").click(function() {
            deleteProjectConfig();
        });

        // project specific setting
        $('#jitlab-commit').on('change', function() {
            var id = $("#jitlab-project").val();
            config.projectConfigs[id].commit = this.value.trim();
        });

        $('#jitlab-merge1').on('change', function() {
            var id = $('#jitlab-project').val();
            config.projectConfigs[id].mergeOpen = this.value.trim();
        });

        $('#jitlab-merge2').on('change', function() {
            var id = $('#jitlab-project').val();
            config.projectConfigs[id].mergeReopen = this.value.trim();
        });

        $('#jitlab-merge3').on('change', function() {
            var id = $('#jitlab-project').val();
            config.projectConfigs[id].mergeMerge = this.value.trim();
        });

        $('#jitlab-merge4').on('change', function() {
            var id = $('#jitlab-project').val();
            config.projectConfigs[id].mergeClose = this.value.trim();
        });

        $('#jitlab-merge5').on('change', function() {
            var id = $('#jitlab-project').val();
            config.projectConfigs[id].mergeApprove = this.value.trim();
        });

        $('#jitlab-commit-transitions').on('change', function() {
            var id = $('#jitlab-project').val();
            config.projectConfigs[id].commitTransitions = this.value.trim();
        });

        $('jitlab-merge1-transitions').on('change', function() {
            var id = $('#jitlab-project').val();
            config.projectConfigs[id].mergeOpenTransitions = this.value.trim();
        });

        $('#jitlab-merge2-transitions').on('change', function() {
            var id = $('#jitlab-project').val();
            config.projectConfigs[id].mergeReopenTransitions = this.value.trim();
        });

        $('#jitlab-merge3-transitions').on('change', function() {
            var id = $('#jitlab-project').val();
            config.projectConfigs[id].mergeMergeTransitions = this.value.trim();
        });

        $('#jitlab-merge4-transitions').on('change', function() {
            var id = $('#jitlab-project').val();
            config.projectConfigs[id].mergeCloseTransitions = this.value.trim();
        });

        $('#jitlab-merge5-transitions').on('change', function() {
            var id = $('#jitlab-project').val();
            config.projectConfigs[id].mergeApproveTransitions = this.value.trim();
        });

        $("#jitlab-link-commit").change(function() {
            var id = $('#jitlab-project').val();
            config.projectConfigs[id].linkCommit = $(this).is(':checked');
        });

        $("#jitlab-link-merge").change(function() {
            var id = $('#jitlab-project').val();
            config.projectConfigs[id].linkMerge = $(this).is(':checked');
        });
    }

    function validate() {
        return validateToken();
    }

    function validateToken() {
        if ((new RegExp("^[a-zA-Z0-9]{" + tokenSize + ",100}$")).test($("#jitlab-token").val())) {
            $("#jitlab-token-error").hide();
            return true;
        }

        $("#jitlab-token-error").show();
        return false;
    }

    function generateToken() {
        var result = "";
        var base = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        for (var i = 0; i < tokenSize; i++) {
          result += base.charAt(Math.floor(Math.random() * base.length));
        }
        $("#jitlab-token").val(result);
    }

    function addProjectOption(key, title) {
        AJS.$("#jitlab-project").append("<option value='" + key + "'>" + title + "</option>");
    }

    function newProject(localId, typeId, fromId) {
        localId = localId.trim().toLowerCase();
        // TODO use configuration type
        if (localId === "" || !isNormalInteger(localId)) {
            JIRA.Messages.showWarningMsg(AJS.I18n.getText("jitlab-connect.admin.projects.project.error.gitlabprojectid"));
            return false;
        }

        var id = localId + ";" + typeId;
        if (AJS.$("#jitlab-project option[value='"+ id + "']").length > 0) {
            JIRA.Messages.showWarningMsg(AJS.I18n.getText("jitlab-connect.admin.projects.project.error.alreadyexist"));
            return false;
        }

        var gitlab = AJS.I18n.getText("jitlab-connect.admin.projects.project.gitlab");
        var title = gitlab + localId;

        addProjectOption(id, title);

        // add to cache
        config.projectConfigs[id] = jQuery.extend(true, {}, config.projectConfigs[fromId]);
        config.projectConfigs[id].title = title;
        config.projectConfigs[id].localId=localId;
        config.projectConfigs[id].type=typeId;

        $("#jitlab-project").val(id).change();
        return true;
    }

// todo
// 2. add expanded with description
// 3. validate project id
// -5. use selector for actions

    function deleteProjectConfig() {
        if (AJS.$("#jitlab-project").val() === "") {
            JIRA.Messages.showWarningMsg(AJS.I18n.getText("jitlab-connect.admin.projects.project.error.deletedefault"));
            return;
        }

        // delete
        delete config.projectConfigs[AJS.$("#jitlab-project").val()];
        AJS.$("#jitlab-project option:selected").remove();
        switchProject()
    }

    function switchProject() {
        $("#jitlab-project-title").text($("#jitlab-project :selected").text())
        var projectConfig = config.projectConfigs[AJS.$("#jitlab-project").val()];

        // load to controls
        $("#jitlab-commit").val(projectConfig.commit).change();
        $("#jitlab-merge1").val(projectConfig.mergeOpen).change();
        $("#jitlab-merge2").val(projectConfig.mergeReopen).change();
        $("#jitlab-merge3").val(projectConfig.mergeMerge).change();
        $("#jitlab-merge4").val(projectConfig.mergeClose).change();
        $("#jitlab-merge5").val(projectConfig.mergeApprove).change();
        $("#jitlab-commit-transitions").val(projectConfig.commitTransitions).change();
        $("#jitlab-merge1-transitions").val(projectConfig.mergeOpenTransitions).change();
        $("#jitlab-merge2-transitions").val(projectConfig.mergeReopenTransitions).change();
        $("#jitlab-merge3-transitions").val(projectConfig.mergeMergeTransitions).change();
        $("#jitlab-merge4-transitions").val(projectConfig.mergeCloseTransitions).change();
        $("#jitlab-merge5-transitions").val(projectConfig.mergeApproveTransitions).change();
        $("#jitlab-link-commit").attr('checked', projectConfig.linkCommit);
        $("#jitlab-link-merge").attr('checked', projectConfig.linkMerge);
    }

    function updateConfig() {
        AJS.$.ajax({
            url: restUrl,
            type: "PUT",
            contentType: "application/json",
            data: JSON.stringify(config),
            processData: false,
            success: function(data) {
                if (data.status == "ok") {
                    JIRA.Messages.showSuccessMsg(data.message);
                } else {
                    JIRA.Messages.showErrorMsg(data.message);
                }
            },
            error: function (error) {
                JIRA.Messages.showErrorMsg(error);
            }
        });
    }

    function isNormalInteger(str) {
        return /^\+?(0|[1-9]\d*)$/.test(str);
    }
})(AJS.$ || jQuery);


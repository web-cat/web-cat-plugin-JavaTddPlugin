#========================================================================
package Web_CAT::Maria;
#========================================================================

use warnings;
use strict;
use Exporter;
use vars qw(@ISA @EXPORT_OK);


@ISA = qw(Exporter);
@EXPORT_OK = qw(
   explainButton
   talkBubble
   chatbox
   dialog
);

our $VERSION = 0.1;


#========================================================================
#                      -----  PUBLIC METHODS -----
#========================================================================

#========================================================================
sub explainButton
{
    my $id = shift;
    my $enabled = shift;
    my $title = shift;
    if (defined $enabled && !$enabled) { return ''; }
    if (defined $title)
    {
        $title =~ s/"/&quot;/g;
        $title = ' data-title="' . $title . '"';
    }
    else
    {
        $title = '';
    }

    return <<RESPONSE;
<button data-issue="$id"$title class="explainError btn btn-secondary btn-sm">Explain...</button>
RESPONSE
}


#========================================================================
sub talkBubble
{
    my $msg = shift;
    my $enabled = shift;
    if (defined $enabled && !$enabled) { return ''; }

    return <<RESPONSE;
<div class="module flex">
  <div class="talkbubble">
    $msg
  </div>
  <img class="vta sm" width="70" height="84"
    src="\${pluginResource:JavaTddPlugin}/maria-sm.png"/>
</div>
RESPONSE
}


#========================================================================
sub dialog
{
    my $enabled = shift;
    if (defined $enabled && !$enabled) { return ''; }

    return <<RESPONSE;
<div id="MariaExplanationDialog" dojoType="webcat.Dialog" title="Here's What I Found">
  <div class="flex">
    <div id="modal-text"></div>
      <img class="vta sm" width="70" height="84"
        src="\${pluginResource:JavaTddPlugin}/maria-sm.png"/>
  </div>
  <div style="text-align: center">
    <button id="explainokbutton" dojoType="dijit.form.Button" type="button" class="pos">OK
      <script type="dojo/connect" event="onClick">
        dijit.byId('MariaExplanationDialog').hide();
      </script>
    </button>
  </div>
</div>
RESPONSE
}


#========================================================================
sub chatbox
{
    my $enabled = shift;
    if (defined $enabled && !$enabled) { return ''; }

    return <<RESPONSE;
<div id="live-chat">
  <header class="clearfix" id="chat-header">
    <h4>Ask a TA</h4><span class="chat-message-counter"></span>
  </header>
  <div class="chat" style="display:flex;">
    <div id="avatar-here"></div>
    <div>
      <div class="chat-history" id="chat-history">
        <div class="chat-bubble chat-message-rcvd clearfix">
          Hello! Give me just one second!
        </div>
        <ul id="choices"></ul>
      </div>
      <form action="#" method="post" id="chatForm">
        <fieldset>
          <input type="text" name="input-chatbox" id="chatbox-input"
            placeholder="Type your message" autocomplete="off" autofocus/>
          <input type="hidden"/>
        </fieldset>
      </form>
    </div>
  </div>
</div>
RESPONSE
}


# ---------------------------------------------------------------------------
1;
# ---------------------------------------------------------------------------


